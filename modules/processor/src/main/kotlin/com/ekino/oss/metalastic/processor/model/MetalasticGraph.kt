/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.processor.model

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import org.springframework.data.elasticsearch.annotations.Document

class MetalasticGraph {
  private val models = mutableSetOf<MetaClassModel>()

  fun models(): Set<MetaClassModel> = models

  fun documentModels() = models.asSequence().filterIsInstance<DocumentClass>()

  fun objectModels() = models.asSequence().filterIsInstance<ObjectClass>()

  fun nestedModels() = models.asSequence().filter { it.isNested }

  fun rootModels() = models.asSequence().filterNot { it.isNested }

  fun getModel(classDeclaration: KSClassDeclaration?): MetaClassModel? =
    classDeclaration?.let {
      models.find {
        it.sourceClassDeclaration.qualifiedName?.asString() ==
          classDeclaration.qualifiedName?.asString()
      }
    }

  open inner class GraphMember {
    val graph
      get() = this@MetalasticGraph
  }

  inner class DocumentClass(
    override val parentModel: MetaClassModel? = null,
    override val sourceClassDeclaration: KSClassDeclaration,
    override val qClassName: String,
    override val fields: List<FieldModel>,
  ) : GraphMember(), MetaClassModel {

    init {
      models.add(this)
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is DocumentClass) return false

      return fullyQualifiedName == other.fullyQualifiedName
    }

    override fun hashCode(): Int {
      return fullyQualifiedName.hashCode()
    }

    /** The companion property name (lowercase simple class name). */
    val companionPropertyName: String
      get() = sourceClassDeclaration.simpleName.asString().replaceFirstChar { it.lowercase() }

    /** The Elasticsearch index name extracted from @Document annotation. */
    val indexName: String
      get() = sourceClassDeclaration.getAnnotationsByType(Document::class).first().indexName
  }

  inner class ObjectClass(
    override val parentModel: MetaClassModel? = null,
    override val sourceClassDeclaration: KSClassDeclaration,
    override val qClassName: String,
    override val fields: List<FieldModel>,
  ) : GraphMember(), MetaClassModel {

    init {
      models.add(this)
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is ObjectClass) return false

      return fullyQualifiedName == other.fullyQualifiedName
    }

    override fun hashCode(): Int {
      return fullyQualifiedName.hashCode()
    }
  }

  sealed interface MetaClassModel {
    val parentModel: MetaClassModel?
    val sourceClassDeclaration: KSClassDeclaration
    val qClassName: String
    val fields: List<FieldModel>
    val graph: MetalasticGraph

    val sourceParentClass
      get() = sourceClassDeclaration.parentDeclaration as? KSClassDeclaration

    val packageName
      get() = sourceClassDeclaration.packageName.asString()

    val isNested: Boolean
      get() = sourceParentClass != null

    val fullyQualifiedName: String
      get() =
        when (val parent = parentModel) {
          null -> "${sourceClassDeclaration.qualifiedName!!.getQualifier()}.$qClassName"
          else -> "${parent.fullyQualifiedName}.$qClassName"
        }

    val qualifier: String
      get() =
        fullyQualifiedName.removePrefix(
          packageName.takeUnless { it.isBlank() }?.plus(".").orEmpty()
        )

    fun nestedClasses() = graph.models().filter { it.parentModel == this }
  }
}
