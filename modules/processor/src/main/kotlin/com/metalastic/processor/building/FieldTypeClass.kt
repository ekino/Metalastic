package com.metalastic.processor.building

import com.squareup.kotlinpoet.ClassName

/**
 * Field type mapping for generating core classes.
 *
 * @param className The ClassName of the field
 * @see [com.metalastic.core.Field]
 */
@JvmInline
value class FieldTypeClass(val className: ClassName) {

  val helperMethodName
    get() = className.simpleName.replaceFirstChar { it.lowercase() }.removeSuffix("Field")
}
