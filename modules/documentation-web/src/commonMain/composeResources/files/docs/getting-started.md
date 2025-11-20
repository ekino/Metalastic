# Getting Started

Get Metalastic running in your project in 5 minutes.

## Prerequisites

- Kotlin 2.2.0+
- Gradle 8.0+
- Spring Data Elasticsearch project

## Step 1: Add Plugin

Add to your `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
    id("com.ekino.oss.metalastic") version "1.0.0"
}
```

## Step 2: Configure

```kotlin
metalastic {
    metamodels {
        packageName = "com.example.search"
        className = "Metamodels"
        classPrefix = "Meta"
    }
}
```

## Step 3: Build

```bash
./gradlew build
```

That's it! Your metamodels are now generated in `build/generated/ksp/main/kotlin/`.

## What's Generated?

For each `@Document` annotated class, Metalastic generates:

- **Metamodel class** with type-safe field accessors
- **Centralized registry** in the `Metamodels` object
- **Path traversal** support for nested objects

## Example

```kotlin
import com.example.MetaProduct.Companion.product

// Type-safe field access
product.title.path() // "title"
product.price.path() // "price"

// Nested paths
product.category.name.path() // "category.name"
```

## Next Steps

- Configure field types
- Explore the Query DSL
- Learn about path traversal
