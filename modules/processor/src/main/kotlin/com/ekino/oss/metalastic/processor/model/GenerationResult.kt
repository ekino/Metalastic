package com.metalastic.processor.model

import com.squareup.kotlinpoet.FileSpec

/** Immutable result of the BUILDING phase containing all generated KotlinPoet specifications. */
data class GenerationResult(val qClasses: List<FileSpec>, val metamodels: FileSpec?)
