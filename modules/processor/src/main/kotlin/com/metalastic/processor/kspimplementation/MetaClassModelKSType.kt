package com.metalastic.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance

/** Synthetic KSType for generated Q-classes. */
class MetaClassModelKSType(private val decl: MetaClassModelClassDeclaration) : KSType {
  override val annotations: Sequence<KSAnnotation> = emptySequence()

  override val arguments: List<KSTypeArgument> by lazy {
    // Q-classes have a single type parameter T, so we create a type argument for it
    val typeParameter = decl.typeParameters.first() // T
    val typeReference = SyntheticKSTypeReference(typeParameter)
    listOf(SyntheticKSTypeArgument(typeReference, Variance.INVARIANT))
  }

  override val declaration: KSDeclaration = decl

  override val isError: Boolean = false

  override val isFunctionType: Boolean = false

  override val isMarkedNullable: Boolean = false

  override val isSuspendFunctionType: Boolean = false

  override val nullability: Nullability = Nullability.NOT_NULL

  override fun isAssignableFrom(that: KSType): Boolean {
    // Q-classes are only assignable from themselves
    return when (that) {
      is MetaClassModelKSType,
      is MetaClassModelKSTypeWithCustomArguments ->
        this.decl.model.fullyQualifiedName ==
          (that.declaration as MetaClassModelClassDeclaration).model.fullyQualifiedName
      else -> false
    }
  }

  override fun isCovarianceFlexible(): Boolean = false

  override fun isMutabilityFlexible(): Boolean = false

  override fun makeNotNullable(): KSType = this

  override fun makeNullable(): KSType = MetaClassModelKSTypeNullable(decl)

  override fun replace(arguments: List<KSTypeArgument>): KSType {
    return when (arguments.size) {
      0 -> this // No replacement needed
      1 -> {
        // Create a new MetaClassModelKSType with the replaced type argument
        // This supports type argument substitution for the single type parameter T
        MetaClassModelKSTypeWithCustomArguments(decl, arguments)
      }
      else ->
        throw UnsupportedOperationException(
          "Q-classes only support exactly one type argument, got ${arguments.size}: ${decl.model.qClassName}"
        )
    }
  }

  override fun starProjection(): KSType = this

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MetaClassModelKSType) return false
    return decl.model.fullyQualifiedName == other.decl.model.fullyQualifiedName
  }

  override fun hashCode(): Int = decl.model.fullyQualifiedName.hashCode()

  override fun toString(): String = "MetaClassModelKSType(${decl.model.fullyQualifiedName})"
}
