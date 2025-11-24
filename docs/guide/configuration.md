<script setup>
import { data as v } from '../.vitepress/versions.data'
</script>

# Configuration

Configure Metalastic metamodel generation using the type-safe Gradle plugin DSL or manual KSP configuration.

## Understanding What Gets Generated

Before configuring Metalastic, it's important to understand what artifacts are generated and how configuration affects them.

### Two Types of Generated Artifacts

Metalastic generates two distinct types of files:

#### 1. Individual Metamodel Classes

One class per `@Document` annotated class:

```
Source Document                    Generated Metamodel
────────────────────────────────  ─────────────────────────────────
com.example.domain.Product    →   com.example.domain.MetaProduct
com.example.domain.Category   →   com.example.domain.MetaCategory
com.example.domain.Review     →   com.example.domain.MetaReview
```

**Key points:**
- ✅ Stay in the **same package** as the source document
- ✅ Named with **configurable prefix** (default: `"Meta"`)
- ✅ Controlled by `classPrefix` configuration

#### 2. Centralized Registry Class

One registry class per source set containing all metamodel references:

```
Generated Registry
──────────────────────────────────────────
com.example.search.Metamodels
```

```kotlin
object Metamodels {
    fun entries(): Sequence<Document<*>> = sequenceOf(
        product,   // from MetaProduct.Companion
        category,  // from MetaCategory.Companion
        review     // from MetaReview.Companion
    )
}
```

**Key points:**
- ✅ Package controlled by `packageName` configuration
- ✅ Class name controlled by `registryClassName` configuration
- ✅ One registry per source set (main, test, etc.)

## Configuration Properties

### Quick Reference Table

| Property | Controls | Applies To | Default | Example |
|----------|----------|------------|---------|---------|
| `packageName` | Package of **registry class** | Metamodels registry only | Auto-detected, fallback: `"com.ekino.oss.metalastic"` | `"com.example.search"` |
| `registryClassName` | Name of **registry class** | Metamodels registry only | `"Metamodels"` | `"MainMetamodels"` |
| `classPrefix` | Prefix for **Meta* classes** | All document metamodels | `"Meta"` | `"Models"` |

::: warning Important
`registryClassName` only controls the **centralized registry class name**, not individual document metamodel names. Use `classPrefix` to control document class prefixes (MetaProduct, MetaCategory, etc.).
:::

### Detailed Property Explanations

#### `packageName`

**Controls:** Package of the centralized Metamodels registry class

```kotlin
metalastic {
    metamodels {
        packageName = "com.example.search.metamodels"
    }
}
```

**Generates:**
```kotlin
// File: com/example/search/metamodels/Metamodels.kt
package com.example.search.metamodels

object Metamodels {
    fun entries(): Sequence<Document<*>> = ...
}
```

**Auto-detection behavior:**

If not explicitly configured, `packageName` is determined by:

1. **Single package:** If all `@Document` classes are in one package → use that package
2. **Multiple packages:** Find common ancestor package
   - Example: `com.example.domain` + `com.example.model` → `com.example`
3. **No common ancestor or empty:** Fallback to `"com.ekino.oss.metalastic"`

**Individual Meta* classes:** Stay in their original document packages (NOT affected by this setting)

#### `registryClassName`

**Controls:** Name of the centralized Metamodels registry class

```kotlin
metalastic {
    metamodels {
        registryClassName = "MainMetamodels"
    }
}
```

**Generates:**
```kotlin
object MainMetamodels {  // ← Name controlled by registryClassName
    fun entries(): Sequence<Document<*>> = ...
}
```

**Individual Meta* classes:** NOT affected (use `classPrefix` instead)

#### `classPrefix`

**Controls:** Prefix added to all generated document metamodel class names

```kotlin
metalastic {
    metamodels {
        classPrefix = "Models"
    }
}
```

**Generates:**
```
Product    → ModelsProduct
Category   → ModelsCategory
Review     → ModelsReview
```

Default is `"Meta"` (MetaProduct, MetaCategory, etc.)

## Basic Configuration

### Simple Single Source Set

For projects with just a main source set:

```kotlin-vue
plugins {
    id("com.google.devtools.ksp") version "{{ v.ksp }}"
    id("com.ekino.oss.metalastic") version "{{ v.metalastic }}"
}

metalastic {
    metamodels {
        packageName = "com.example.search"
        registryClassName = "Metamodels"
        classPrefix = "Meta"  // default, can be omitted
    }
}
```

**Generates:**
- Registry: `com.example.search.Metamodels`
- Documents: `com.example.domain.MetaProduct`, etc. (in their original packages)

### Multi-Source Set Configuration

For projects with main, test, and custom source sets:

```kotlin-vue
metalastic {
    metamodels {
        // Global defaults (apply to all source sets unless overridden)
        classPrefix = "Meta"

        // Main source set specific
        main {
            packageName = "com.example.main.search"
            registryClassName = "MainMetamodels"
        }

        // Test source set specific
        test {
            packageName = "com.example.test.search"
            registryClassName = "TestMetamodels"
        }

        // Custom source set (use sourceSet syntax)
        sourceSet("integration") {
            packageName = "com.example.integration.search"
            registryClassName = "IntegrationMetamodels"
        }
    }
}
```

**Generates:**
- Main registry: `com.example.main.search.MainMetamodels`
- Test registry: `com.example.test.search.TestMetamodels`
- Integration registry: `com.example.integration.search.IntegrationMetamodels`
- All document classes: Use `Meta` prefix (inherited from global `classPrefix`)

## Source Set Configuration

### Pre-defined Source Sets

Metalastic provides convenient accessors for common source sets:

```kotlin-vue
metalastic {
    metamodels {
        main {
            // Main application code
        }

        test {
            // Unit tests
        }

        // Integration test variants
        integration {
            // Integration tests
        }

        integrationTest {
            // Alternative naming
        }

        // Functional test variants
        functional {
            // Functional tests
        }

        functionalTest {
            // Alternative naming
        }

        // E2E test variants
        e2e {
            // End-to-end tests
        }

        e2eTest {
            // Alternative naming
        }
    }
}
```

### Custom Source Sets

For custom source sets, use the `sourceSet()` function:

```kotlin-vue
metalastic {
    metamodels {
        sourceSet("contractTest") {
            packageName = "com.example.contract"
            registryClassName = "ContractMetamodels"
        }

        sourceSet("performance") {
            packageName = "com.example.perf"
            registryClassName = "PerformanceMetamodels"
        }
    }
}
```

## Features Configuration

### Java Compatibility

Generate `@JvmField` annotations for seamless Java interoperability:

```kotlin-vue
metalastic {
    features {
        generateJavaCompatibility = true  // default: true
    }
}
```

**When enabled:**
```kotlin
@JvmField
val id: KeywordField<String> = keyword("id")
```

**When disabled:**
```kotlin
val id: KeywordField<String> = keyword("id")  // No @JvmField
```

**Recommendation:** Keep enabled unless you're in a Kotlin-only project and want to reduce annotation clutter.

### Private Class Metamodels

Generate metamodels for private `@Document` classes:

```kotlin-vue
metalastic {
    features {
        generatePrivateClassMetamodels = false  // default: false
    }
}
```

**When disabled (default):**
```kotlin
@Document(indexName = "internal")
private class InternalDoc { }  // No MetaInternalDoc generated
```

**When enabled:**
```kotlin
@Document(indexName = "internal")
private class InternalDoc { }  // Generates MetaInternalDoc
```

**Recommendation:** Keep disabled unless you specifically need metamodels for private classes. Public API is cleaner without them.

## Debug Reporting

### Enable Processor Reports

Generate detailed markdown reports for debugging and build analysis:

```kotlin-vue
metalastic {
    reporting {
        enabled = true  // default: false
        outputPath = "build/reports/metalastic/processor-report.md"
    }
}
```

**Report includes:**
- ⏱️ Performance metrics for each processing phase
- 📊 Generation statistics (documents discovered, fields processed)
- 🔍 Detailed processing log with timestamps
- ❌ Error diagnostics with context
- 📈 Historical tracking across builds

**When to use:**
- 🐛 Debugging generation issues
- ⚡ Optimizing build performance
- 📊 Understanding what's being generated
- 🚀 Monitoring in CI/CD pipelines

## Manual Configuration (Without Gradle Plugin)

If not using the Gradle plugin, configure KSP directly:

```kotlin-vue
plugins {
    id("com.google.devtools.ksp") version "{{ v.ksp }}"
}

dependencies {
    implementation("com.ekino.oss:metalastic-core:{{ v.metalastic }}")
    ksp("com.ekino.oss:metalastic-processor:{{ v.metalastic }}")
}

ksp {
    // Global configuration
    arg("metamodels.package", "com.example.search")
    arg("metamodels.registryClassName", "Metamodels")
    arg("metamodels.classPrefix", "Meta")

    // Source set specific (use dot notation)
    arg("metamodels.main.package", "com.example.main")
    arg("metamodels.main.registryClassName", "MainMetamodels")

    arg("metamodels.test.package", "com.example.test")
    arg("metamodels.test.registryClassName", "TestMetamodels")

    // Features
    arg("metalastic.generateJavaCompatibility", "true")
    arg("metalastic.generatePrivateClassMetamodels", "false")

    // Reporting
    arg("metalastic.reportingPath", "build/reports/metalastic/report.md")
}
```

**KSP Argument Keys:**

| Gradle Plugin DSL | KSP Argument Key |
|-------------------|------------------|
| `metamodels.packageName` | `metamodels.package` |
| `metamodels.registryClassName` | `metamodels.registryClassName` |
| `metamodels.classPrefix` | `metamodels.classPrefix` |
| `metamodels.main.packageName` | `metamodels.main.package` |
| `metamodels.main.registryClassName` | `metamodels.main.registryClassName` |
| `features.generateJavaCompatibility` | `metalastic.generateJavaCompatibility` |
| `features.generatePrivateClassMetamodels` | `metalastic.generatePrivateClassMetamodels` |
| `reporting.enabled + outputPath` | `metalastic.reportingPath` |

## Complete Configuration Reference

### Full Gradle Plugin Configuration

```kotlin-vue
metalastic {
    // Metamodel generation configuration
    metamodels {
        // ────────────────────────────────────────────────────────
        // Global defaults (apply to all source sets)
        // ────────────────────────────────────────────────────────
        packageName = "com.example.search"    // Registry package
        registryClassName = "Metamodels"              // Registry class name
        classPrefix = "Meta"                  // Meta* prefix

        // ────────────────────────────────────────────────────────
        // Source set specific overrides
        // ────────────────────────────────────────────────────────
        main {
            packageName = "com.example.main.search"
            registryClassName = "MainMetamodels"
            classPrefix = "Meta"  // Can override per source set
        }

        test {
            packageName = "com.example.test.search"
            registryClassName = "TestMetamodels"
        }

        // Custom source sets
        sourceSet("integration") {
            packageName = "com.example.integration"
            registryClassName = "IntegrationMetamodels"
        }
    }

    // ────────────────────────────────────────────────────────
    // Feature flags
    // ────────────────────────────────────────────────────────
    features {
        generateJavaCompatibility = true       // @JvmField annotations
        generatePrivateClassMetamodels = false // Skip private classes
    }

    // ────────────────────────────────────────────────────────
    // Debug reporting
    // ────────────────────────────────────────────────────────
    reporting {
        enabled = false  // Enable for debugging
        outputPath = "build/reports/metalastic/processor-report.md"
    }
}
```

## Next Steps

- [Understanding Metamodels](/guide/understanding-metamodels) - Learn how generation works
- [Field Types Reference](/guide/field-types-reference) - Explore all supported field types
- [Query DSL Guide](/guide/query-dsl-guide) - Build type-safe queries
