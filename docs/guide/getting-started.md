<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Getting Started

Get Metalastic running in your project in 5 minutes.

## Prerequisites

- Kotlin {{ v.kotlin }}+
- Gradle 8.0+
- Spring Data Elasticsearch project

## Step 1: Add Dependencies

::: code-group

```kotlin-vue [Gradle Plugin (Recommended)]
plugins {
    id("com.google.devtools.ksp") version "{{ v.ksp }}"
    id("com.ekino.oss.metalastic") version "{{ v.metalastic }}"
}

metalastic {
    metamodels {
        packageName = "com.example.search"
        className = "Metamodels"
        classPrefix = "Meta"  // Default
    }
}
```

```kotlin-vue [Direct Dependencies]
plugins {
    id("com.google.devtools.ksp") version "{{ v.ksp }}"
}

dependencies {
    implementation("com.ekino.oss:metalastic-core:{{ v.metalastic }}")
    ksp("com.ekino.oss:metalastic-processor:{{ v.metalastic }}")
}

ksp {
    arg("metamodels.package", "com.example.search")
    arg("metamodels.className", "Metamodels")
    arg("metamodels.classPrefix", "Meta")
}
```

:::

The Gradle plugin provides a type-safe DSL and automatic KSP configuration.

## Step 2: Build

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
