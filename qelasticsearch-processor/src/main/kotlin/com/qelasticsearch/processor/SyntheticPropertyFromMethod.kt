package com.qelasticsearch.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSPropertyGetter
import com.google.devtools.ksp.symbol.KSPropertySetter
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin

/**
 * A synthetic KSPropertyDeclaration created from a getter method. This wrapper allows us to process
 * getter methods using the existing property processing logic.
 */
class SyntheticPropertyFromMethod(
  private val method: KSFunctionDeclaration,
  private val propertyName: String,
) : KSPropertyDeclaration {

  override val annotations: Sequence<KSAnnotation>
    get() = method.annotations

  override val containingFile: KSFile?
    get() = method.containingFile

  override val docString: String?
    get() = method.docString

  override val location: Location
    get() = method.location

  override val modifiers: Set<Modifier>
    get() = method.modifiers

  override val origin: Origin
    get() = method.origin

  override val parent: KSNode?
    get() = method.parent

  override val parentDeclaration: KSDeclaration?
    get() = method.parentDeclaration

  override val qualifiedName: KSName?
    get() = method.qualifiedName

  override val packageName: KSName
    get() = method.packageName

  override val simpleName: KSName
    get() =
      method.containingFile?.packageName?.let { KSNameImpl.getCached(propertyName) }
        ?: KSNameImpl.getCached(propertyName)

  override val typeParameters: List<KSTypeParameter>
    get() = emptyList()

  override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
    return visitor.visitPropertyDeclaration(this, data)
  }

  override fun findOverridee(): KSPropertyDeclaration? = null

  override val extensionReceiver: KSTypeReference?
    get() = null

  override val getter: KSPropertyGetter?
    get() = null

  override val hasBackingField: Boolean
    get() = false

  override val isMutable: Boolean
    get() = false

  override val setter: KSPropertySetter?
    get() = null

  override val type: KSTypeReference
    get() = method.returnType!!

  override fun asMemberOf(containing: KSType): KSType = method.returnType!!.resolve()

  override fun isDelegated(): Boolean = false

  override fun findActuals(): Sequence<KSDeclaration> = emptySequence()

  override fun findExpects(): Sequence<KSDeclaration> = emptySequence()

  override val isActual: Boolean = false

  override val isExpect: Boolean = false
}

/** Simple implementation of KSName for the synthetic property */
private class KSNameImpl private constructor(private val name: String) : KSName {
  override fun asString(): String = name

  override fun getQualifier(): String = ""

  override fun getShortName(): String = name

  companion object {
    private val cache = mutableMapOf<String, KSNameImpl>()

    fun getCached(name: String): KSNameImpl = cache.getOrPut(name) { KSNameImpl(name) }
  }
}
