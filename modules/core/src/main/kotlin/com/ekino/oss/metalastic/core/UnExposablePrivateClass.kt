/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.metalastic.core

/**
 * Marker object representing a private class that cannot be exposed in generated metamodel code.
 *
 * This object is used as a type placeholder in generated metamodels when the annotation processor
 * encounters private classes that would cause compilation errors if referenced directly in the
 * generated code. Instead of using `Any`, this specific marker type provides clear indication of
 * what was replaced and why.
 *
 * **Use Cases:**
 * - Private classes used as field types in `@Document` classes
 * - Private classes used as generic type arguments (e.g., `List<PrivateClass>`)
 * - Private inner classes that are inaccessible to consumers
 *
 * **Benefits over using `Any`:**
 * - Clear indication that a private class was encountered
 * - Makes generated code more self-documenting
 * - Helps developers understand why certain types appear in metamodels
 * - Type-safe marker that's easily recognizable
 *
 * @see com.ekino.oss.metalastic.processor.collecting.toSafeTypeName
 * @see com.ekino.oss.metalastic.processor.collecting.withSafeGenericArguments
 */
object UnExposablePrivateClass
