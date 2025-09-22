package com.metalastic.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.NonExistLocation
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Origin

/**
 * Synthetic KSTypeReference implementation for Q-class type references.
 *
 * This represents type references in Q-class type arguments, such as the `T` in `QExample<T>` where
 * the Q-class extends `Document<T : Any>` or `ObjectField<T>`.
 *
 * @param typeParameter The type parameter this reference points to
 */
class SyntheticKSTypeReference(private val typeParameter: KSTypeParameter) : KSTypeReference {
  override val annotations: Sequence<KSAnnotation> = emptySequence()

  override val element: KSReferenceElement? = null

  override val modifiers: Set<Modifier> = emptySet()

  override val location: Location = NonExistLocation

  override val origin: Origin = Origin.SYNTHETIC

  override val parent: KSNode? = null

  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
    throw UnsupportedOperationException("SyntheticKSTypeReference does not support visitors")
  }

  override fun resolve(): KSType = SyntheticKSType(typeParameter)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SyntheticKSTypeReference) return false
    return typeParameter == other.typeParameter
  }

  override fun hashCode(): Int = typeParameter.hashCode()

  override fun toString(): String = "SyntheticKSTypeReference($typeParameter)"
}

/**
 * Synthetic KSType implementation for type parameters in Q-classes.
 *
 * This represents the resolved type of a type parameter like `T` in Q-class generics.
 */
private class SyntheticKSType(private val typeParameter: KSTypeParameter) : KSType {
  override val annotations: Sequence<KSAnnotation> = emptySequence()

  override val arguments: List<KSTypeArgument> = emptyList()

  override val declaration: com.google.devtools.ksp.symbol.KSDeclaration = typeParameter

  override val isError: Boolean = false

  override val isFunctionType: Boolean = false

  override val isMarkedNullable: Boolean = false

  override val isSuspendFunctionType: Boolean = false

  override val nullability: Nullability = Nullability.NOT_NULL

  override fun isAssignableFrom(that: KSType): Boolean = this == that

  override fun isCovarianceFlexible(): Boolean = false

  override fun isMutabilityFlexible(): Boolean = false

  override fun makeNotNullable(): KSType = this

  override fun makeNullable(): KSType = SyntheticKSTypeNullable(typeParameter)

  override fun replace(arguments: List<KSTypeArgument>): KSType {
    if (arguments.isNotEmpty()) {
      throw UnsupportedOperationException("Type parameters don't support type arguments")
    }
    return this
  }

  override fun starProjection(): KSType = this

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SyntheticKSType) return false
    return typeParameter == other.typeParameter
  }

  override fun hashCode(): Int = typeParameter.hashCode()

  override fun toString(): String = "SyntheticKSType($typeParameter)"
}

/** Nullable version of SyntheticKSType. */
private class SyntheticKSTypeNullable(private val typeParameter: KSTypeParameter) : KSType {
  override val annotations: Sequence<KSAnnotation> = emptySequence()

  override val arguments: List<KSTypeArgument> = emptyList()

  override val declaration: com.google.devtools.ksp.symbol.KSDeclaration = typeParameter

  override val isError: Boolean = false

  override val isFunctionType: Boolean = false

  override val isMarkedNullable: Boolean = true

  override val isSuspendFunctionType: Boolean = false

  override val nullability: Nullability = Nullability.NULLABLE

  override fun isAssignableFrom(that: KSType): Boolean {
    return when (that) {
      is SyntheticKSType,
      is SyntheticKSTypeNullable -> this.typeParameter == (that.declaration as KSTypeParameter)
      else -> false
    }
  }

  override fun isCovarianceFlexible(): Boolean = false

  override fun isMutabilityFlexible(): Boolean = false

  override fun makeNotNullable(): KSType = SyntheticKSType(typeParameter)

  override fun makeNullable(): KSType = this

  override fun replace(arguments: List<KSTypeArgument>): KSType {
    if (arguments.isNotEmpty()) {
      throw UnsupportedOperationException("Type parameters don't support type arguments")
    }
    return this
  }

  override fun starProjection(): KSType = this

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SyntheticKSTypeNullable) return false
    return typeParameter == other.typeParameter
  }

  override fun hashCode(): Int = typeParameter.hashCode() + 1

  override fun toString(): String = "SyntheticKSTypeNullable($typeParameter)"
}
