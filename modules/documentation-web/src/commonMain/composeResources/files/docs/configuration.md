# Configuration

Configure Metalastic using the Gradle plugin DSL.

## Basic Configuration

```kotlin
metalastic {
    metamodels {
        packageName = "com.example.search"
        className = "Metamodels"
        classPrefix = "Meta"
    }
}
```

## Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `packageName` | String | auto-detected | Package for generated metamodels |
| `className` | String | "Metamodels" | Name of the registry class |
| `classPrefix` | String | "Meta" | Prefix for metamodel classes |

## Source Set Specific Configuration

Configure different settings per source set:

```kotlin
metalastic {
    metamodels {
        // Global defaults
        classPrefix = "Meta"

        // Main source set
        main {
            packageName = "com.example.main"
            className = "MainMetamodels"
        }

        // Test source set
        test {
            packageName = "com.example.test"
            className = "TestMetamodels"
        }
    }
}
```

## Features

### Generate Java Compatibility

Add `@JvmField` annotations for Java interop:

```kotlin
metalastic {
    features {
        generateJavaCompatibility = true  // default: true
    }
}
```

### Private Class Metamodels

Generate metamodels for private classes:

```kotlin
metalastic {
    features {
        generatePrivateClassMetamodels = false  // default: false
    }
}
```

## Debug Reporting

Enable processor reports for debugging:

```kotlin
metalastic {
    reporting {
        enabled = true
        outputPath = "build/reports/metalastic/report.md"
    }
}
```

## Manual Configuration (without plugin)

If not using the Gradle plugin:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
}

dependencies {
    implementation("com.ekino.oss:metalastic-core:1.0.0")
    ksp("com.ekino.oss:metalastic-processor:1.0.0")
}

ksp {
    arg("metamodels.package", "com.example.search")
    arg("metamodels.className", "Metamodels")
    arg("metamodels.classPrefix", "Meta")
}
```

## Next Steps

- Explore field types
- Use the Query DSL
