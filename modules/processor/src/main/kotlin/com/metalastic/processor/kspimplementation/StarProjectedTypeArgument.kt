package com.metalastic.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Variance

/**
 * A type argument wrapper that forces star projection variance (`*`).
 *
 * This class delegates all functionality to the source type argument except for variance, which is
 * always set to [Variance.STAR]. This is used to convert complex generic types to star-projected
 * equivalents for cleaner code generation.
 *
 * @param source The original type argument to wrap
 */
class StarProjectedTypeArgument(source: KSTypeArgument) : KSTypeArgument by source {
  override val variance: Variance
    get() = Variance.STAR
}
