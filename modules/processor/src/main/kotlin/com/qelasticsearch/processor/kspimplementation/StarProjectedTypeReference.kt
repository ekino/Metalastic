package com.qelasticsearch.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.qelasticsearch.processor.collecting.withStarProjectedGenericArguments

/**
 * A type reference that applies star projection transformation when resolved.
 *
 * This class wraps a source type reference and ensures that:
 * 1. Its reference element uses star projections
 * 2. When resolved, it applies [withStarProjectedGenericArguments] recursively
 *
 * This enables deep transformation of complex nested generic types.
 *
 * @param source The original type reference to wrap
 */
class StarProjectedTypeReference(val source: KSTypeReference) : KSTypeReference by source {
  override val element: KSReferenceElement? =
    source.element?.let { StarProjectedReferenceElement(it) }

  override fun resolve(): KSType = source.resolve().withStarProjectedGenericArguments()
}
