package com.metalastic.processor.kspimplementation

import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance

/**
 * Factory functions for creating synthetic KSP implementations.
 *
 * These functions provide convenient factory methods for creating synthetic KSP objects used in
 * Metalastic metamodel generation.
 */

/**
 * Creates a simple unbounded type parameter.
 *
 * @param name The name of the type parameter (e.g., "T", "E", "K", "V")
 * @return A [SyntheticKSTypeParameter] representing an unbounded type parameter
 */
fun createTypeParameter(name: String): SyntheticKSTypeParameter = SyntheticKSTypeParameter(name)

/**
 * Creates a bounded type parameter with upper bounds.
 *
 * @param name The name of the type parameter
 * @param bounds List of upper bound type references
 * @param variance The variance of the type parameter
 * @param isReified Whether the type parameter is reified
 * @return A [SyntheticKSTypeParameter] with the specified constraints
 */
fun createBoundedTypeParameter(
  name: String,
  bounds: List<KSTypeReference>,
  variance: Variance = Variance.INVARIANT,
  isReified: Boolean = false,
): SyntheticKSTypeParameter = SyntheticKSTypeParameter(name, bounds, variance, isReified)
