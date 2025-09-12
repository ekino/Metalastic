package com.qelasticsearch.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSName

/** Synthetic KSName implementation for Q-class names. */
class QClassModelKSName(private val name: String) : KSName {
  init {
    require(name.isNotBlank()) { "Name cannot be blank" }
  }

  override fun asString(): String = name

  override fun getQualifier(): String = name.substringBeforeLast('.')

  override fun getShortName(): String = name.substringAfterLast('.')

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is KSName) return false
    return name == other.asString()
  }

  override fun hashCode(): Int = name.hashCode()

  override fun toString(): String = "QClassModelKSName($name)"
}
