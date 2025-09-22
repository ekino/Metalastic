package com.qelasticsearch.processor.kspimplementation

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin
import com.qelasticsearch.processor.collecting.withStarProjectedGenericArguments
import com.qelasticsearch.processor.model.ElasticsearchGraph

/**
 * Synthetic KSClassDeclaration for generated Q-classes.
 *
 * This allows us to create a KSType for Q-classes that don't exist yet, enabling proper type
 * resolution with TypeParameterResolver.
 */
class QClassModelClassDeclaration(val model: ElasticsearchGraph.QClassModel) :
  KSClassDeclaration by model.sourceClassDeclaration {
  override val classKind: ClassKind = ClassKind.CLASS

  override val isCompanionObject: Boolean = false

  override val primaryConstructor: KSFunctionDeclaration? = null

  override val superTypes: Sequence<KSTypeReference> = emptySequence()

  override fun asStarProjectedType(): KSType =
    QClassModelKSType(this).withStarProjectedGenericArguments()

  override fun asType(typeArguments: List<KSTypeArgument>): KSType =
    if (typeArguments.isEmpty()) QClassModelKSType(this)
    else
      throw UnsupportedOperationException(
        "Q-classes don't support type arguments: ${model.qClassName}"
      )

  override fun getAllFunctions(): Sequence<KSFunctionDeclaration> = emptySequence()

  override fun getAllProperties(): Sequence<KSPropertyDeclaration> = emptySequence()

  override fun getSealedSubclasses(): Sequence<KSClassDeclaration> = emptySequence()

  override val docString: String =
    "Generated Q-class for ${model.sourceClassDeclaration.qualifiedName?.asString()}"

  override val packageName: KSName = QClassModelKSName(model.packageName)

  override val parentDeclaration: KSDeclaration? =
    model.parentModel?.let { QClassModelClassDeclaration(it) }

  override val qualifiedName: KSName = QClassModelKSName(model.fullyQualifiedName)

  override val simpleName: KSName = QClassModelKSName(model.qClassName)

  override val typeParameters: List<KSTypeParameter> = listOf(createTypeParameter("T"))

  override val modifiers: Set<Modifier> = setOf(Modifier.PUBLIC)

  override val origin: Origin = Origin.SYNTHETIC

  override val parent: KSNode? = parentDeclaration

  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
    throw UnsupportedOperationException("QClassModelClassDeclaration does not support visitors")
  }

  override val annotations: Sequence<KSAnnotation> = emptySequence()

  override val isActual: Boolean = false

  override val isExpect: Boolean = false

  override fun findActuals(): Sequence<KSDeclaration> = emptySequence()

  override fun findExpects(): Sequence<KSDeclaration> = emptySequence()

  override val declarations: Sequence<KSDeclaration> = emptySequence()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is QClassModelClassDeclaration) return false
    return model.fullyQualifiedName == other.model.fullyQualifiedName
  }

  override fun hashCode(): Int = model.fullyQualifiedName.hashCode()

  override fun toString(): String = "QClassModelClassDeclaration(${model.fullyQualifiedName})"
}
