/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.processor.model

import com.squareup.kotlinpoet.FileSpec

/** Immutable result of the BUILDING phase containing all generated KotlinPoet specifications. */
data class GenerationResult(val qClasses: List<FileSpec>, val metamodels: FileSpec?)
