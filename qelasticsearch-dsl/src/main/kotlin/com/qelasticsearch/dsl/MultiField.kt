package com.qelasticsearch.dsl

abstract class MultiField<T : Field>(
    field: T,
) : FieldContainer(),
    Field by field
