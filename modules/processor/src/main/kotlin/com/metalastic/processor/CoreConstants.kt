package com.metalastic.processor

import com.metalastic.core.Document
import com.metalastic.core.Metamodel
import com.metalastic.core.MultiField
import com.metalastic.core.ObjectField
import com.metalastic.core.SelfReferencingObject
import com.metalastic.core.UnModellableObject

/** Constants for Core logic generation. */
object CoreConstants {
  const val PRODUCT_NAME = "Metalastic"
  const val CORE_PACKAGE = "com.metalastic.core"
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
      const val FALLBACK_METAMODELS_PACKAGE = "com.metalastic"
      const val PACKAGE_OVERRIDE = "metamodels.package"
      const val CLASS_NAME = "metamodels.className"
      const val CLASS_PREFIX = "metamodels.classPrefix"
    }
  }
}
