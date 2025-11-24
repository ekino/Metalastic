/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.processor

import com.ekino.oss.metalastic.core.Document
import com.ekino.oss.metalastic.core.Metamodel
import com.ekino.oss.metalastic.core.MultiField
import com.ekino.oss.metalastic.core.ObjectField
import com.ekino.oss.metalastic.core.SelfReferencingObject
import com.ekino.oss.metalastic.core.UnModellableObject

/** Constants for Core logic generation. */
object CoreConstants {
  const val PRODUCT_NAME = "Metalastic"
  const val CORE_PACKAGE = "com.ekino.oss.metalastic.core"
  const val META_PREFIX = "Meta"
  const val MULTIFIELD_POSTFIX = "MultiField"

  object MetaModelClass {
    val SIMPLE_NAME = Metamodel::class.simpleName!!
    val NAME_PROPERTY = PropertyConst("name")
  }

  object ObjectFieldClass {
    val SIMPLE_NAME = ObjectField::class.simpleName!!
    val NAME_PROPERTY = MetaModelClass.NAME_PROPERTY
    val PARENT_PROPERTY = PropertyConst("parent")
    val NESTED_PROPERTY = PropertyConst("nested")
    val FIELD_TYPE_PROPERTY = PropertyConst("fieldType")
  }

  object DocumentClass {
    val SIMPLE_NAME = Document::class.simpleName!!
    const val INDEX_NAME_FUNCTION = "indexName"
    const val INDEX_NAME_CONSTANT = "INDEX_NAME"
  }

  object MultiFieldClass {
    val SIMPLE_NAME = MultiField::class.simpleName!!
    val PARENT_PROPERTY = ObjectFieldClass.PARENT_PROPERTY
    const val MAIN_FIELD_PROPERTY = "mainFieldName"
  }

  object UnModellableObjectClass {
    val SIMPLE_NAME = UnModellableObject::class.simpleName!!
  }

  object SelfReferencingObjectClass {
    val SIMPLE_NAME = SelfReferencingObject::class.simpleName!!
  }

  object Metamodels {
    const val SIMPLE_NAME = "Metamodels"
    const val ENTRIES_FUNCTION_NAME = "entries"
  }

  const val DOCUMENT_ANNOTATION = "org.springframework.data.elasticsearch.annotations.Document"
  const val SPRING_DATA_ELASTICSEARCH_PACKAGE = "org.springframework.data.elasticsearch.annotations"

  object MethodPrefixes {
    const val GET = "get"
    const val IS = "is"
  }

  object ProcessorOptions {
    const val GENERATE_JAVA_COMPATIBILITY = "metalastic.generateJavaCompatibility"
    const val GENERATE_PRIVATE_CLASS_METAMODELS = "metalastic.generatePrivateClassMetamodels"
    const val REPORTING_PATH = "metalastic.reportingPath"

    object Metamodels {
      const val FALLBACK_METAMODELS_PACKAGE = "com.ekino.oss.metalastic"

      // Key components for composition
      private const val KEY_PREFIX = "metamodels"
      private const val PACKAGE_SUFFIX = "package"
      private const val REGISTRY_CLASS_NAME_SUFFIX = "registryClassName"
      private const val CLASS_PREFIX_SUFFIX = "classPrefix"

      // Global KSP argument keys (composed)
      const val PACKAGE_OVERRIDE = "$KEY_PREFIX.$PACKAGE_SUFFIX"
      const val REGISTRY_CLASS_NAME = "$KEY_PREFIX.$REGISTRY_CLASS_NAME_SUFFIX"
      const val CLASS_PREFIX = "$KEY_PREFIX.$CLASS_PREFIX_SUFFIX"

      // Source-set specific key components (public for ProcessorOptions)
      const val SOURCE_SET_KEY_PREFIX = "$KEY_PREFIX."
      const val SOURCE_SET_PACKAGE_SUFFIX = ".$PACKAGE_SUFFIX"
      const val SOURCE_SET_REGISTRY_CLASS_NAME_SUFFIX = ".$REGISTRY_CLASS_NAME_SUFFIX"
      const val SOURCE_SET_CLASS_PREFIX_SUFFIX = ".$CLASS_PREFIX_SUFFIX"
    }
  }
}
