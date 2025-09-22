package com.qelasticsearch.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance

/**
 * A type reference wrapper that applies star projection to nested generic arguments.
 *
 * This class is part of the star projection transformation chain. It wraps a source type argument
 * and ensures that when its type reference is accessed, it returns a [StarProjectedTypeReference]
 * that continues the star projection transformation.
 *
 * @param source The original type argument to wrap
 */
class StarProjectedKSTypeReference(val source: KSTypeArgument) :
  KSTypeArgument, KSAnnotated by source {
  override val type: KSTypeReference? = source.type?.let { StarProjectedTypeReference(it) }
  override val variance: Variance = source.variance
}
