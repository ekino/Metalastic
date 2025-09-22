package com.metalastic.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.metalastic.processor.collecting.asStarProjectedTypeArgument

/**
 * A reference element that applies star projection to all type arguments.
 *
 * This is the final piece of the star projection transformation chain. It ensures that all type
 * arguments in the reference element are converted to star projections, enabling clean generation
 * of generic types in metamodel classes.
 *
 * **Example transformation:**
 * - Input: `Map<String, List<T>>`
 * - Output: `Map<String, List<*>>`
 *
 * @param source The original reference element to wrap
 */
class StarProjectedReferenceElement(val source: KSReferenceElement) : KSReferenceElement by source {
  override val typeArguments: List<KSTypeArgument> =
    source.typeArguments.map { it.asStarProjectedTypeArgument() }
}
