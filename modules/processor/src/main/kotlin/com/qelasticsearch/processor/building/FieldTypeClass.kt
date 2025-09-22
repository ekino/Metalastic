package com.qelasticsearch.processor.building

import com.squareup.kotlinpoet.ClassName

/**
 * Field type mapping for generating core classes.
 *
 * @param className The ClassName of the field
 * @see [com.qelasticsearch.core.Field]
 */
@JvmInline
value class FieldTypeClass(val className: ClassName) {

  val helperMethodName
    get() = className.simpleName.replaceFirstChar { it.lowercase() }.removeSuffix("Field")
}
