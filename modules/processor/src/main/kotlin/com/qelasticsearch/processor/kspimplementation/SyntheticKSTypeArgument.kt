package com.qelasticsearch.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.NonExistLocation
import com.google.devtools.ksp.symbol.Origin
import com.google.devtools.ksp.symbol.Variance

/**
 * Synthetic KSTypeArgument implementation for Q-class type arguments.
 *
 * This represents type arguments like `<T>` in generated Q-classes such as `QExample<T>` that
 * extend `Document<T : Any>` or `ObjectField<T>`.
 *
 * @param typeReference The type reference for this type argument (e.g., T, String, etc.)
 * @param variance The variance of this type argument (INVARIANT, COVARIANT, CONTRAVARIANT)
 */
class SyntheticKSTypeArgument(
  private val typeReference: KSTypeReference,
  private val argumentVariance: Variance = Variance.INVARIANT,
) : KSTypeArgument {
  override val type: KSTypeReference = typeReference

  override val variance: Variance = argumentVariance

  override val annotations: Sequence<KSAnnotation> = emptySequence()

  override val location: Location = NonExistLocation

  override val origin: Origin = Origin.SYNTHETIC

  override val parent: KSNode? = null

  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
    throw UnsupportedOperationException("SyntheticKSTypeArgument does not support visitors")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SyntheticKSTypeArgument) return false
    return type == other.type && variance == other.variance
  }

  override fun hashCode(): Int = type.hashCode() * 31 + variance.hashCode()

  override fun toString(): String = "SyntheticKSTypeArgument(type=$type, variance=$variance)"
}
