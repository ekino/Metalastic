package com.qelasticsearch.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance

/** Nullable version of QClassModelKSType */
class QClassModelKSTypeNullable(private val decl: QClassModelClassDeclaration) : KSType {
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

  override val isMarkedNullable: Boolean = true

  override val isSuspendFunctionType: Boolean = false

  override val nullability: Nullability = Nullability.NULLABLE

  override fun isAssignableFrom(that: KSType): Boolean {
    return when (that) {
      is QClassModelKSType,
      is QClassModelKSTypeNullable,
      is QClassModelKSTypeWithCustomArguments,
      is QClassModelKSTypeNullableWithCustomArguments ->
        this.decl.model.fullyQualifiedName ==
          (that.declaration as QClassModelClassDeclaration).model.fullyQualifiedName
      else -> false
    }
  }

  override fun isCovarianceFlexible(): Boolean = false

  override fun isMutabilityFlexible(): Boolean = false

  override fun makeNotNullable(): KSType = QClassModelKSType(decl)

  override fun makeNullable(): KSType = this

  override fun replace(arguments: List<KSTypeArgument>): KSType {
    return when (arguments.size) {
      0 -> this // No replacement needed
      1 -> {
        // Create a new nullable QClassModelKSType with the replaced type argument
        QClassModelKSTypeNullableWithCustomArguments(decl, arguments)
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
    if (other !is QClassModelKSTypeNullable) return false
    return decl.model.fullyQualifiedName == other.decl.model.fullyQualifiedName
  }

  override fun hashCode(): Int = decl.model.fullyQualifiedName.hashCode() + 1

  override fun toString(): String = "QClassModelKSTypeNullable(${decl.model.fullyQualifiedName})"
}
