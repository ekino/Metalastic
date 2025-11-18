# Metalastic Gradle Plugin

A Gradle plugin that provides automatic dependency management and type-safe DSL configuration for the Metalastic annotation processor.

## Features

- ✅ **Automatic dependency management** - Adds both core and processor dependencies
- ✅ **Type-safe DSL configuration** - Configure with IDE autocompletion
- ✅ **Multiple source set support** - Built-in shortcuts for common source sets
- ✅ **Dynamic source set configuration** - Support for custom source set names
- ✅ **Version consistency** - Ensures matching core and processor versions
- ✅ **Zero classpath conflicts** - No KSP version conflicts
- ✅ **Minimal setup** - Just apply the plugin and configure

## Quick Start

### 1. Apply Plugins

```kotlin
plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
    id("com.metalastic") version "local-SNAPSHOT"  // Development version
}
```

### 2. Configure (Optional)

```kotlin
metalastic {
    metamodels {
        main {
            packageName = "com.example.metamodels"  // Default: ${project.group}.metamodels
            className = "SearchMetamodels"           // Default: "Metamodels"
        }
    }

    features {
        generateJavaCompatibility = true             // Default: true
        generatePrivateClassMetamodels = false      // Default: false
    }

    reporting {
        enabled = true                               // Default: false
        outputPath = "build/reports/metalastic/report.md"
    }
}
```

### 3. Build

```bash
./gradlew build
```

That's it! The plugin automatically:

- Adds `com.metalastic:core` to your implementation dependencies
- Adds `com.metalastic:processor` to your KSP dependencies
- Configures KSP with your settings
- Generates metamodel classes for your `@Document` annotated classes

## Configuration Reference

### Metamodels Configuration

Configure where generated metamodel classes are placed. The plugin supports both built-in shortcuts for common source sets and dynamic configuration for custom source sets.

#### Built-in Source Set Shortcuts

The plugin provides convenient shortcuts for these common source sets:

- `main` - Main source set (default: `${project.group}.metamodels`)
- `test` - Test source set
- `integration` - Integration tests
- `integrationTest` - Integration test source set
- `functional` - Functional tests
- `functionalTest` - Functional test source set
- `e2e` - End-to-end tests
- `e2eTest` - End-to-end test source set

#### Dynamic Source Set Configuration

For custom source sets, use the `sourceSet("name")` method:

```kotlin
metalastic {
    metamodels {
        // Standard shortcuts for common source sets
        main {
            packageName = "com.example.search"      // Package for generated classes
            className = "SearchMetamodels"           // Registry class name
        }
        test {
            packageName = "com.example.test"        // Test package (optional)
            className = "TestMetamodels"             // Test registry (optional)
        }
        integration {
            packageName = "com.example.integration" // Integration package (optional)
            className = "IntegrationMetamodels"     // Integration registry (optional)
        }
        integrationTest {
            packageName = "com.example.integrationtest"
            className = "IntegrationTestMetamodels"
        }
        functional {
            packageName = "com.example.functional"
            className = "FunctionalMetamodels"
        }
        functionalTest {
            packageName = "com.example.functionaltest"
            className = "FunctionalTestMetamodels"
        }
        e2e {
            packageName = "com.example.e2e"
            className = "E2EMetamodels"
        }
        e2eTest {
            packageName = "com.example.e2etest"
            className = "E2ETestMetamodels"
        }

        // Dynamic source set support for custom configurations
        sourceSet("performance") {
            packageName = "com.example.performance"
            className = "PerformanceMetamodels"
        }
        sourceSet("contract") {
            packageName = "com.example.contract"
            className = "ContractMetamodels"
        }

        // Global defaults (optional)
        packageName = "com.example.default"
        className = "DefaultMetamodels"
    }
}
```

### Features Configuration

Control code generation behavior:

```kotlin
metalastic {
    features {
        generateJavaCompatibility = true       // Generate @JvmField annotations
        generatePrivateClassMetamodels = false // Process private classes
    }
}
```

### Reporting Configuration

Generate processing reports:

```kotlin
metalastic {
    reporting {
        enabled = true                        // Enable report generation
        outputPath = "build/reports/metalastic/report.md"
    }
}
```

## Examples

### Minimal Setup

```kotlin
plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
    id("com.metalastic") version "local-SNAPSHOT"
}

// No configuration needed - uses sensible defaults
// Generated metamodels will be in: ${project.group}.metamodels
```

### Custom Package

```kotlin
metalastic {
    metamodels {
        main {
            packageName = "com.mycompany.search.models"
            className = "ElasticMetamodels"
        }
    }
}
```

### Java-First Project

```kotlin
metalastic {
    features {
        generateJavaCompatibility = true  // Adds @JvmField for Java interop
    }
}
```

### With Reporting

```kotlin
metalastic {
    reporting {
        enabled = true
        outputPath = "build/reports/metalastic/processing-report.md"
    }
}
```

### Multi-Module Project with Different Source Sets

```kotlin
metalastic {
    metamodels {
        main {
            packageName = "com.company.core.search"
            className = "CoreMetamodels"
        }
        integration {
            packageName = "com.company.integration.search"
            className = "IntegrationMetamodels"
        }
        e2e {
            packageName = "com.company.e2e.search"
            className = "E2EMetamodels"
        }

        // Custom source sets for specific testing needs
        sourceSet("performance") {
            packageName = "com.company.performance.search"
            className = "PerformanceMetamodels"
        }
        sourceSet("contract") {
            packageName = "com.company.contract.search"
            className = "ContractMetamodels"
        }
    }
}
```

### Microservice with Simplified Configuration

```kotlin
metalastic {
    metamodels {
        // Only configure what you need - other shortcuts remain unused
        main {
            packageName = "com.service.billing.search"
            className = "BillingMetamodels"
        }
        integrationTest {
            packageName = "com.service.billing.integration"
            className = "BillingIntegrationMetamodels"
        }
    }
}
```

## Generated Output

The plugin generates two types of files:

### 1. Document Metamodel Classes

For each `@Document` class, generates a corresponding `Q*` class:

```kotlin
// For: @Document class UserDocument
class QUserDocument(
    parent: ObjectField? = null,
    fieldName: String = "",
    nested: Boolean = false,
) : Index("user_index", parent, fieldName, nested) {
    @JvmField
    val id: KeywordField<String> = keywordField<String>("id")

    @JvmField
    val name: TextField<String> = textField<String>("name")
}
```

### 2. Centralized Registry

A single registry object for discovering all metamodels:

```kotlin
// Generated in configured package (e.g., com.example.metamodels)
import com.ekino.oss.metalastic.core.Document
import com.example.QUserDocument.Companion.userDocument
import com.example.QProductDocument.Companion.productDocument
import jakarta.annotation.Generated
import kotlin.jvm.JvmStatic
import kotlin.sequences.Sequence

@Generated("com.ekino.oss.metalastic.processor.MetalasticSymbolProcessor", date="...")
object Metamodels {
    /**
     * Returns a sequence of all generated metamodel instances.
     */
    @JvmStatic
    fun entries(): Sequence<Document<*>> = sequenceOf(
        userDocument,
        productDocument,
        // ... other documents
    )
}
```

Individual metamodel instances are accessed via companion objects:

```kotlin
import com.example.QUserDocument.Companion.userDocument

// Use directly
userDocument.name.path() shouldBe "name"
```

## Troubleshooting

### KSP Plugin Not Found

```
Metalastic: KSP plugin not found. Please add it to your plugins block
```

**Solution**: Add the KSP plugin:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
}
```

### Version Mismatch

Ensure your KSP version is compatible with your Kotlin version:

- Kotlin 2.2.20 → KSP 2.2.20-2.0.3
- Kotlin 2.2.10 → KSP 2.2.10-2.0.2

### Generated Files Not Found

1. Check that your classes have `@Document` annotations
2. Run `./gradlew kspKotlin` to trigger generation
3. Look in `build/generated/ksp/main/kotlin/` for generated files

## Requirements

- Gradle 8.0+
- Kotlin 2.2.0+
- Java 21+
- Spring Data Elasticsearch (for `@Document` annotations)

## Version Compatibility

| Plugin Version | Kotlin Version | KSP Version  |
| -------------- | -------------- | ------------ |
| 2.0.x          | 2.2.20         | 2.2.20-2.0.3 |
| 1.9.x          | 2.2.10         | 2.2.10-2.0.2 |

## Contributing

This plugin is part of the Metalastic project. See the main project README for contribution guidelines.
