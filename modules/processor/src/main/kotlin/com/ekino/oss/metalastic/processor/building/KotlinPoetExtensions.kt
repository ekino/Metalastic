package com.ekino.oss.metalastic.processor.building

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeSpec
import jakarta.annotation.Generated
import java.time.OffsetDateTime

/** Creates the @Generated annotation for Q-classes. */
fun TypeSpec.Builder.addGeneratedAnnotation() = apply {
  val currentTime = OffsetDateTime.now().toString()
  addAnnotation(
    AnnotationSpec.builder(Generated::class)
      .addMember("%S, date=%S", QClassGenerator.SYMBOL_PROCESSOR_FQN, currentTime)
      .build()
  )
}
