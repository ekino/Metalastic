package com.qelasticsearch.processor

import com.squareup.kotlinpoet.CodeBlock

@JvmInline
value class PropertyConst(val name: String) {

  infix fun valued(propertyValue: String) = CodeBlock.of("%L = %S", name, propertyValue)

  val namedArgument: CodeBlock
    get() = CodeBlock.of("%L = %L", name, name)
}
