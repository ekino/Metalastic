package com.qelasticsearch.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.NonExistLocation
import com.google.devtools.ksp.symbol.Origin
import com.google.devtools.ksp.symbol.Variance

/**
 * A synthetic implementation of [KSTypeParameter] for representing generic type parameters.
 *
 * This class is useful when you need to create artificial type parameters for code generation, such
 * as representing `T` in `MyClass<T>` or creating bounded type parameters like `T : Any`.
 *
 * **Use Cases in QElasticsearch:**
 * - Creating generic type parameters for generated metamodel classes
 * - Representing bounded type parameters with upper bounds
 * - Building synthetic type hierarchies for complex generic scenarios
 *
 * **Example Usage:**
 *
 * ```kotlin
 * // Simple unbounded type parameter: T
 * val typeParamT = SyntheticKSTypeParameter("T")
 *
 * // Bounded type parameter: T : Any
 * val boundedTypeParam = SyntheticKSTypeParameter("T", listOf(anyTypeReference))
 *
 * // Multiple bounds: T : Serializable & Comparable<T>
 * val multiBounded = SyntheticKSTypeParameter("T", listOf(serializableRef, comparableRef))
 * ```
 *
 * @param parameterName The name of the type parameter (e.g., "T", "E", "K", "V")
 * @param upperBounds List of upper bound type references. Empty list means no bounds.
 * @param variance The variance of the type parameter (IN, OUT, or INVARIANT)
 * @param isReified Whether this type parameter is reified (typically false for synthetic ones)
 */
class SyntheticKSTypeParameter(
  private val parameterName: String,
  private val upperBounds: List<KSTypeReference> = emptyList(),
  override val variance: Variance = Variance.INVARIANT,
  override val isReified: Boolean = false,
) : KSTypeParameter {

  // Core type parameter properties
  override val name: KSName =
    object : KSName {
      override fun asString(): String = parameterName

      override fun getQualifier(): String = ""

      override fun getShortName(): String = parameterName
    }

  override val bounds: Sequence<KSTypeReference> = upperBounds.asSequence()

  // KSDeclaration properties
  override val annotations: Sequence<KSAnnotation> = emptySequence()
  override val modifiers: Set<Modifier> = emptySet()
  override val simpleName: KSName = name
  override val qualifiedName: KSName? = name
  override val containingFile: KSFile? = null
  override val docString: String?
    get() = null

  override val packageName: KSName =
    object : KSName {
      override fun asString(): String = ""

      override fun getQualifier(): String = ""

      override fun getShortName(): String = ""
    }
  override val parentDeclaration: KSDeclaration? = null
  override val typeParameters: List<KSTypeParameter> = emptyList()

  // KSNode properties
  override val location: Location = NonExistLocation
  override val origin: Origin = Origin.SYNTHETIC
  override val parent: KSNode? = null

  // Visitor pattern
  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
    return visitor.visitTypeParameter(this, data)
  }

  // String representation for debugging
  override fun toString(): String {
    val boundsStr =
      if (upperBounds.isNotEmpty()) {
        " : ${upperBounds.joinToString(" & ") { it.toString() }}"
      } else ""

    val varianceStr =
      when (variance) {
        Variance.COVARIANT -> "out "
        Variance.CONTRAVARIANT -> "in "
        else -> ""
      }

    val reifiedStr = if (isReified) "reified " else ""

    return "$reifiedStr$varianceStr$parameterName$boundsStr"
  }

  override val isActual: Boolean
    get() = false

  override val isExpect: Boolean
    get() = false

  override fun findActuals(): Sequence<KSDeclaration> = emptySequence()

  override fun findExpects(): Sequence<KSDeclaration> = emptySequence()
}
