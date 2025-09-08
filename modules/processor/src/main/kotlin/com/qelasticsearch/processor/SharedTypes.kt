package com.qelasticsearch.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import org.springframework.data.elasticsearch.annotations.FieldType

/**
 * Enhanced context for import management with conflict detection and package proximity
 * prioritization.
 */
class ImportContext(
  private val currentPackage: String,
  val usedImports: MutableSet<String> = mutableSetOf(),
) {
  private val typeUsages: MutableMap<String, MutableSet<String>> = mutableMapOf()
  private val prioritizedTypes: MutableMap<String, String> = mutableMapOf()
  private val resolutionCache: MutableMap<String, String> = mutableMapOf()
  private val locallyDefinedNestedClasses: MutableSet<String> = mutableSetOf()

  /** Register a type that will be used in the generated code. */
  fun registerTypeUsage(qualifiedName: String) {
    val simpleName = qualifiedName.substringAfterLast('.')
    typeUsages.computeIfAbsent(simpleName) { mutableSetOf() }.add(qualifiedName)
  }

  /** Register a nested class that is defined locally in the current document. */
  fun registerLocallyDefinedNestedClass(qualifiedName: String) {
    locallyDefinedNestedClasses.add(qualifiedName)
  }

  /** Finalize import decisions based on conflicts and package proximity. */
  fun finalizeImportDecisions() {
    typeUsages.forEach { (simpleName, qualifiedNames) ->
      when {
        qualifiedNames.size > 1 -> handleConflictingTypes(simpleName, qualifiedNames)
        else -> handleSingleType(qualifiedNames.first())
      }
    }
  }

  private fun handleConflictingTypes(simpleName: String, qualifiedNames: Set<String>) {
    // Check if this is a nested class conflict - if so, skip importing entirely
    if (isNestedClassConflict(simpleName)) {
      return // For nested class conflicts, force fully qualified names for all
    }

    // Handle regular conflicts: choose winner (excluding locally defined nested classes)
    val nonLocalQualifiedNames = qualifiedNames.filter { !isLocallyDefinedNestedClass(it) }
    if (nonLocalQualifiedNames.isNotEmpty()) {
      val chosen = prioritizeConflictingType(nonLocalQualifiedNames.toSet())
      prioritizedTypes[simpleName] = chosen

      val chosenPackage = chosen.substringBeforeLast('.')
      if (chosenPackage != currentPackage) {
        usedImports.add(chosen)
      }
    }
  }

  private fun handleSingleType(qualifiedName: String) {
    // Handle non-conflicts: single type gets imported if cross-package and not locally defined
    val packageName = qualifiedName.substringBeforeLast('.')
    if (packageName != currentPackage && !isLocallyDefinedNestedClass(qualifiedName)) {
      usedImports.add(qualifiedName)
    }
  }

  /** Get the optimal type name representation for code generation. */
  fun getOptimalTypeName(qualifiedName: String): String {
    return resolutionCache.computeIfAbsent(qualifiedName) { resolveOptimalName(it) }
  }

  private fun resolveOptimalName(qualifiedName: String): String {
    val simpleName = qualifiedName.substringAfterLast('.')
    val packageName = qualifiedName.substringBeforeLast('.')

    return when {
      packageName == currentPackage -> {
        // Same package - check if it's a locally defined nested class
        if (isLocallyDefinedNestedClass(qualifiedName)) {
          // For locally defined nested classes, use simple name without import
          simpleName
        } else {
          // For regular same-package classes, use simple name
          simpleName
        }
      }
      qualifiedName in usedImports -> {
        // Already imported, can use simple name
        simpleName
      }
      prioritizedTypes[simpleName] == qualifiedName -> {
        // Won the priority battle, can use simple name
        simpleName
      }
      else -> {
        // Lost priority battle or not imported, use fully qualified name
        qualifiedName
      }
    }
  }

  private fun prioritizeConflictingType(qualifiedNames: Set<String>): String {
    // Group by priority tiers and find the best within each tier
    val samePackageTypes = qualifiedNames.filter { it.substringBeforeLast('.') == currentPackage }

    val nestedClassTypes = qualifiedNames.filter { isNestedClassInCurrentPackage(it) }

    val crossPackageTypes =
      qualifiedNames.filter {
        !isNestedClassInCurrentPackage(it) && it.substringBeforeLast('.') != currentPackage
      }

    return when {
      samePackageTypes.isNotEmpty() -> samePackageTypes.first()
      nestedClassTypes.isNotEmpty() -> findClosestNestedClass(nestedClassTypes)
      crossPackageTypes.isNotEmpty() -> findClosestPackage(crossPackageTypes)
      else -> qualifiedNames.first()
    }
  }

  private fun findClosestNestedClass(nestedClassTypes: List<String>): String {
    return nestedClassTypes.minByOrNull { calculateNestedClassDepth(it) }
      ?: nestedClassTypes.first()
  }

  private fun findClosestPackage(crossPackageTypes: List<String>): String {
    return crossPackageTypes.minByOrNull { qualifiedName ->
      calculatePackageDistance(currentPackage, qualifiedName.substringBeforeLast('.'))
    } ?: crossPackageTypes.first()
  }

  private fun calculatePackageDistance(fromPackage: String, toPackage: String): Int {
    val fromParts = fromPackage.split(".")
    val toParts = toPackage.split(".")

    var commonLength = 0
    for (i in 0 until minOf(fromParts.size, toParts.size)) {
      if (fromParts[i] == toParts[i]) commonLength++ else break
    }

    return (fromParts.size - commonLength) + (toParts.size - commonLength)
  }

  private fun isNestedClassInCurrentPackage(qualifiedName: String): Boolean {
    return qualifiedName.startsWith("$currentPackage.") &&
      qualifiedName.count { it == '.' } > currentPackage.count { it == '.' } + 1
  }

  private fun calculateNestedClassDepth(qualifiedName: String): Int {
    // Count the number of class separators after the package
    val packagePrefix = "$currentPackage."
    if (!qualifiedName.startsWith(packagePrefix)) return Int.MAX_VALUE

    val classPath = qualifiedName.substring(packagePrefix.length)
    // Count dots in class path to determine nesting depth
    // Fewer dots = less nested = higher priority (lower score)
    return classPath.count { it == '.' }
  }

  private fun isNestedClassConflict(simpleName: String): Boolean {
    val conflictingTypes = typeUsages[simpleName]
    if (conflictingTypes == null || conflictingTypes.size <= 1) return false

    // Check if all conflicting types are nested classes in the current package
    return conflictingTypes.all { qualifiedName ->
      qualifiedName.startsWith("$currentPackage.") &&
        qualifiedName.count { it == '.' } > currentPackage.count { it == '.' } + 1
    }
  }

  private fun isLocallyDefinedNestedClass(qualifiedName: String): Boolean {
    return qualifiedName in locallyDefinedNestedClasses
  }
}

/** Field type mapping for generating DSL classes. */
data class FieldTypeMapping(val className: String)

/** Processed field type information. */
data class ProcessedFieldType(
  val elasticsearchType: FieldType,
  val kotlinType: KSTypeReference,
  val kotlinTypeName: String,
  val isObjectType: Boolean,
)

/** Information about an object field that needs Q-class generation. */
data class ObjectFieldInfo(
  val className: String,
  val packageName: String,
  val classDeclaration: KSClassDeclaration,
  val qualifiedName: String,
  val parentDocumentClass: KSClassDeclaration? = null,
)

/** Constants for Core logic generation. */
object CoreConstants {
  const val CORE_PACKAGE = "com.qelasticsearch.core"
  const val Q_PREFIX = "Q"
  const val INDEX_CLASS = "Index"
  const val OBJECT_FIELDS_CLASS = "ObjectField"
}
