package com.qelasticsearch.processor

import com.qelasticsearch.core.Document
import com.qelasticsearch.core.Field
import com.qelasticsearch.core.MultiField
import com.qelasticsearch.core.ObjectField

/** Constants for Core logic generation. */
object CoreConstants {
  const val PRODUCT_NAME = "QElasticsearch"
  const val CORE_PACKAGE = "com.qelasticsearch.core"
  const val Q_PREFIX = "Q"
  const val MULTIFIELD_POSTFIX = "MultiField"

  object FieldClass {
    val SIMPLE_NAME = Field::class.simpleName!!
    val NAME_PROPERTY = PropertyConst("name")
  }

  object ObjectFieldClass {
    val SIMPLE_NAME = ObjectField::class.simpleName!!
    val NAME_PROPERTY = FieldClass.NAME_PROPERTY
    val PARENT_PROPERTY = PropertyConst("parent")
    val NESTED_PROPERTY = PropertyConst("nested")
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

  object Metamodels {
    const val SIMPLE_NAME = "Metamodels"
    const val ENTRIES_FUNCTION_NAME = "entries"
  }

  const val DOCUMENT_ANNOTATION = "org.springframework.data.elasticsearch.annotations.Document"

  object MethodPrefixes {
    const val GET = "get"
    const val IS = "is"
  }

  object ProcessorOptions {
    const val GENERATE_JAVA_COMPATIBILITY = "qelasticsearch.generateJavaCompatibility"
    const val REPORTING_PATH = "qelasticsearch.reportingPath"

    object Metamodels {
      const val FALLBACK_METAMODELS_PACKAGE = "com.qelasticsearch"
      const val PACKAGE_OVERRIDE = "metamodels.package"
      const val CLASS_NAME = "metamodels.className"
    }
  }
}
