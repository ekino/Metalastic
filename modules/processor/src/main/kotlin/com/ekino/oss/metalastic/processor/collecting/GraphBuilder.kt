package com.ekino.oss.metalastic.processor.collecting

import com.ekino.oss.metalastic.processor.CoreConstants
import com.ekino.oss.metalastic.processor.model.MetalasticGraph
import com.ekino.oss.metalastic.processor.options.ProcessorOptions
import com.ekino.oss.metalastic.processor.report.reporter
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Visibility
import org.springframework.data.elasticsearch.annotations.Document

class GraphBuilder(val resolver: Resolver, val options: ProcessorOptions) {

  fun build(): MetalasticGraph {
    val qClasses = collectQClasses()

    // Resolve class prefix early using the first document class
    val classPrefix =
      qClasses
        .firstOrNull { it.isAnnotationPresent(Document::class) }
        ?.let { resolveClassPrefix(it) } ?: CoreConstants.META_PREFIX

    return qClasses.toGraphWithoutFields(classPrefix).linkModelsAndPopulateFields()
  }

  /**
   * Resolves the class prefix to use for Q-class generation. This method performs early resolution
   * of the classPrefix configuration by detecting the source set from the first document class and
   * applying configuration overrides if available.
   */
  private fun resolveClassPrefix(firstDocumentClass: KSClassDeclaration): String {
    val sourceSet =
      firstDocumentClass.containingFile?.filePath?.let { extractSourceSetFromPath(it) } ?: "main"

    // Check for source-set specific configuration first, then global, then default
    return options.metamodelsConfiguration.sourceSetClassPrefixOverrides[sourceSet]
      ?: options.metamodelsConfiguration.classPrefixOverride
      ?: CoreConstants.META_PREFIX
  }

  /**
   * Extract source set name from file path by finding the directory before 'java' or 'kotlin'.
   * Example: /path/to/src/test/java/com/example/Class.java → "test"
   */
  private fun extractSourceSetFromPath(filePath: String): String? {
    val pathSegments = filePath.split('/')

    // Find the index of 'java' or 'kotlin' directory
    val languageDirIndex = pathSegments.indexOfFirst { it == "java" || it == "kotlin" }
    if (languageDirIndex <= 0) return null // No language dir found or it's the first segment

    // The source set is the directory immediately before the language directory
    return pathSegments[languageDirIndex - 1].takeIf { it.isNotEmpty() }
  }

  /** First pass: Collect all classes that are to become QClasses. */
  private fun collectQClasses(): Collection<KSClassDeclaration> {
    val documents =
      resolver
        .getSymbolsWithAnnotation(CoreConstants.DOCUMENT_ANNOTATION)
        .filterIsInstance<KSClassDeclaration>()
        .filter {
          it.getVisibility() != Visibility.PRIVATE || options.generatePrivateClassMetamodels
        }

    // Use a map to safely compare KSClassDeclaration by fully qualified name
    val foundQClasses = documents.associateBy { it.fullyQualifiedName() }.toMutableMap()
    val classesToExplore = documents.toMutableList()

    fun KSClassDeclaration.addToFoundQClasses() {
      foundQClasses.putIfAbsent(fullyQualifiedName(), this) ?: classesToExplore.add(this)
    }

    generateSequence { classesToExplore.removeFirstOrNull() }
      .forEach { symbol ->
        when (symbol) {
          is KSClassDeclaration -> {
            symbol
              .getAllProperties()
              .filter { it.hasFieldTypeObjectOrNested() }
              .forEach { it.extractPotentialQClass()?.addToFoundQClasses() }
          }
        }
      }
    reporter.debug {
      val classList = foundQClasses.values.map { it.fullyQualifiedName() }.sorted()

      """
      Found ${foundQClasses.values.count()} ${CoreConstants.META_PREFIX} classes:
        ${classList.joinToString(separator = "\n\t")}
      """
        .trim()
    }
    return foundQClasses.values
  }

  /** Second pass: Build QClass models with parent-child relationships, but without fields. */
  private fun Collection<KSClassDeclaration>.toGraphWithoutFields(
    classPrefix: String
  ): MetalasticGraph {
    val graphWithoutFields = MetalasticGraph()

    val modelsByNestingOrder = filter { it.parentDeclaration == null }.toMutableList()

    generateSequence {
        modelsByNestingOrder.removeFirstOrNull()?.also { next ->
          modelsByNestingOrder.addAll(
            filter {
              it.parentDeclaration?.qualifiedName?.asString() == next.qualifiedName?.asString()
            }
          )
        }
      }
      .forEach { qClass ->
        when {
          qClass.isAnnotationPresent(Document::class) -> {
            graphWithoutFields.DocumentClass(
              parentModel =
                (qClass.parentDeclaration as? KSClassDeclaration)?.let {
                  graphWithoutFields.getModel(it)
                },
              sourceClassDeclaration = qClass,
              qClassName = "$classPrefix${qClass.simpleName.asString()}",
              fields = listOf(),
            )
          }

          else -> {
            val isNested = qClass.parentDeclaration != null
            val qClassName =
              if (isNested) {
                qClass.simpleName.asString()
              } else {
                "$classPrefix${qClass.simpleName.asString()}"
              }

            graphWithoutFields.ObjectClass(
              parentModel =
                (qClass.parentDeclaration as? KSClassDeclaration)?.let {
                  graphWithoutFields.getModel(it)
                },
              sourceClassDeclaration = qClass,
              qClassName = qClassName,
              fields = listOf(),
            )
          }
        }
      }

    return graphWithoutFields
  }

  /** Third pass: finalize graph by populating fields. */
  private fun MetalasticGraph.linkModelsAndPopulateFields(): MetalasticGraph {
    val modelsByNestingOrder: MutableList<MetalasticGraph.MetaClassModel> =
      (documentModels().filterNot { it.isNested } + objectModels().filterNot { it.isNested })
        .toMutableList()

    val finalGraph = MetalasticGraph()
    generateSequence {
        modelsByNestingOrder.removeFirstOrNull()?.also {
          modelsByNestingOrder.addAll(it.nestedClasses())
        }
      }
      .forEach { model ->
        val fields = model.collectFields()
        when (model) {
          is MetalasticGraph.DocumentClass ->
            finalGraph.DocumentClass(
              parentModel = model.sourceParentClass?.let { finalGraph.getModel(it) },
              sourceClassDeclaration = model.sourceClassDeclaration,
              qClassName = model.qClassName,
              fields = fields,
            )

          is MetalasticGraph.ObjectClass ->
            finalGraph.ObjectClass(
              parentModel = model.sourceParentClass?.let { finalGraph.getModel(it) },
              sourceClassDeclaration = model.sourceClassDeclaration,
              qClassName = model.qClassName,
              fields = fields,
            )
        }
      }
    return finalGraph
  }
}
