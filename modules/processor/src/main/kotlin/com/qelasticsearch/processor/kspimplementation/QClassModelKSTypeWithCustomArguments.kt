package com.qelasticsearch.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Nullability

/**
 * QClassModelKSType with custom type arguments for type substitution.
 *
 * This class is used when the replace() function is called with new type arguments, allowing for
 * proper type parameter substitution in Q-class types.
 */
internal class QClassModelKSTypeWithCustomArguments(
  private val decl: QClassModelClassDeclaration,
  private val customArguments: List<KSTypeArgument>,
) : KSType {
  override val annotations: Sequence<KSAnnotation> = emptySequence()

  override val arguments: List<KSTypeArgument> = customArguments

  override val declaration: KSDeclaration = decl

  override val isError: Boolean = false

  override val isFunctionType: Boolean = false

  override val isMarkedNullable: Boolean = false

  override val isSuspendFunctionType: Boolean = false

  override val nullability: Nullability = Nullability.NOT_NULL

  override fun isAssignableFrom(that: KSType): Boolean {
    return when (that) {
      is QClassModelKSType,
      is QClassModelKSTypeWithCustomArguments ->
        this.decl.model.fullyQualifiedName ==
          (that.declaration as QClassModelClassDeclaration).model.fullyQualifiedName
      else -> false
    }
  }

  override fun isCovarianceFlexible(): Boolean = false

  override fun isMutabilityFlexible(): Boolean = false

  override fun makeNotNullable(): KSType = this

  override fun makeNullable(): KSType =
    QClassModelKSTypeNullableWithCustomArguments(decl, customArguments)

  override fun replace(arguments: List<KSTypeArgument>): KSType {
    return when (arguments.size) {
      0 -> QClassModelKSType(decl) // Return to default synthetic arguments
      1 -> QClassModelKSTypeWithCustomArguments(decl, arguments)
      else ->
        throw UnsupportedOperationException(
          "Q-classes only support exactly one type argument, got ${arguments.size}: ${decl.model.qClassName}"
        )
    }
  }

  override fun starProjection(): KSType = QClassModelKSType(decl).starProjection()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is QClassModelKSTypeWithCustomArguments) return false
    return decl.model.fullyQualifiedName == other.decl.model.fullyQualifiedName &&
      arguments == other.arguments
  }

  override fun hashCode(): Int =
    decl.model.fullyQualifiedName.hashCode() * 31 + arguments.hashCode()

  override fun toString(): String =
    "QClassModelKSTypeWithCustomArguments(${decl.model.fullyQualifiedName}, $arguments)"
}

/** Nullable QClassModelKSType with custom type arguments for type substitution. */
internal class QClassModelKSTypeNullableWithCustomArguments(
  private val decl: QClassModelClassDeclaration,
  private val customArguments: List<KSTypeArgument>,
) : KSType {
  override val annotations: Sequence<KSAnnotation> = emptySequence()

  override val arguments: List<KSTypeArgument> = customArguments

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

  override fun makeNotNullable(): KSType =
    QClassModelKSTypeWithCustomArguments(decl, customArguments)

  override fun makeNullable(): KSType = this

  override fun replace(arguments: List<KSTypeArgument>): KSType {
    return when (arguments.size) {
      0 -> QClassModelKSTypeNullable(decl) // Return to default synthetic arguments
      1 -> QClassModelKSTypeNullableWithCustomArguments(decl, arguments)
      else ->
        throw UnsupportedOperationException(
          "Q-classes only support exactly one type argument, got ${arguments.size}: ${decl.model.qClassName}"
        )
    }
  }

  override fun starProjection(): KSType = QClassModelKSTypeNullable(decl).starProjection()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is QClassModelKSTypeNullableWithCustomArguments) return false
    return decl.model.fullyQualifiedName == other.decl.model.fullyQualifiedName &&
      arguments == other.arguments
  }

  override fun hashCode(): Int =
    decl.model.fullyQualifiedName.hashCode() * 31 + arguments.hashCode() + 1

  override fun toString(): String =
    "QClassModelKSTypeNullableWithCustomArguments(${decl.model.fullyQualifiedName}, $arguments)"
}
