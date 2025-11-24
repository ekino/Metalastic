# Metalastic v1.1.0 Release Notes

## 🚨 Breaking Changes

### Gradle Plugin API Rename

The `className` property has been renamed to `registryClassName` for better clarity about what it controls.

**Migration Required:**

```kotlin
// Before (v1.0.x)
metalastic {
    metamodels {
        className = "MainMetamodels"
    }
}

// After (v1.1.0)
metalastic {
    metamodels {
        registryClassName = "MainMetamodels"
    }
}
```

**Applies to:**
- Global configuration: `metamodels.registryClassName`
- Source-set specific: `main.registryClassName`, `test.registryClassName`, etc.

**Manual KSP Configuration:**

```kotlin
// Before
ksp {
    arg("metamodels.className", "MainMetamodels")
    arg("metamodels.main.className", "MainMetamodels")
}

// After
ksp {
    arg("metamodels.registryClassName", "MainMetamodels")
    arg("metamodels.main.registryClassName", "MainMetamodels")
}
```

### Why This Change?

The old `className` property was ambiguous - users expected it to control individual Meta* class names (e.g., MetaProduct, MetaCategory). It actually only controls the centralized registry class name (e.g., Metamodels, MainMetamodels).

The new name `registryClassName` makes this explicit and prevents confusion.

## 📚 Documentation Improvements

### Comprehensive Configuration Guide

Major improvements to configuration.md with:

- **"Understanding What Gets Generated" section**
  - Clear distinction between Meta* classes and Metamodels registry

- **Configuration Resolution**
  - Priority table showing resolution order
  - Inheritance visualization
  - Auto-detection behavior with fallback to `"com.ekino.oss.metalastic"`

[Read the improved configuration guide →](https://ekino.github.io/Metalastic/guide/configuration)

### Enhanced Query DSL Documentation

- **StartBound Mathematical Notation** (previously undocumented!)
  - Complete guide to Kotlin range operators (`..` and `..<`)
  - `fromInclusive()` and `fromExclusive()` functions
  - Unbounded ranges with null (`10.fromInclusive()..null`)
  - Comparison table with Guava Range equivalents

- **DSL Syntax Comparison**
  - Operator syntax (`must + { }`) vs Classical syntax (`mustDsl { }`)
  - Side-by-side examples
  - When to use which

- **Built-in DSL Functions**
  - `bool { }` for nested boolean queries
  - `nested { }` for nested field queries
  - Safety validation and logging

[Read the Query DSL Guide →](https://ekino.github.io/Metalastic/guide/query-dsl-guide)

### New "Understanding Metamodels" Guide

- Core concepts and architecture
- How generation works (three-phase processor)
- Type system explanation
- Path traversal
- Metamodels registry

[Read Understanding Metamodels →](https://ekino.github.io/Metalastic/guide/understanding-metamodels)

### Enhanced Field Types Reference

- Spring FieldType → Metalastic mapping table
- Organized by Elasticsearch field categories
- Fixed MultiField documentation to match actual generated code

[Read Field Types Reference →](https://ekino.github.io/Metalastic/guide/field-types-reference)

### Centralized Version Management

- VitePress data loading for single source of truth
- All version numbers managed in one file
- Easier maintenance for future releases

### Simplified README

- Reduced from 1,689 → 253 lines (85% reduction!)
- Focused landing page directing to comprehensive docs site
- Removed QueryDSL references from project description

## 🔧 Code Quality Improvements

### Composed Constants

Constants in CoreConstants are now composed from base components:

```kotlin
object Metamodels {
  // Base components (private)
  private const val KEY_PREFIX = "metamodels"
  private const val REGISTRY_CLASS_NAME_SUFFIX = "registryClassName"

  // Composed global key
  const val REGISTRY_CLASS_NAME = "$KEY_PREFIX.$REGISTRY_CLASS_NAME_SUFFIX"

  // Source-set components (public)
  const val SOURCE_SET_KEY_PREFIX = "$KEY_PREFIX."
  const val SOURCE_SET_REGISTRY_CLASS_NAME_SUFFIX = ".$REGISTRY_CLASS_NAME_SUFFIX"
}
```

**Benefits:**
- ✅ Eliminates string duplication
- ✅ Single source of truth for key structure
- ✅ Easier refactoring
- ✅ Self-documenting

## 📦 Dependencies

Updated dependencies (via Dependabot):
- Various transitive dependency updates for security and compatibility

## 🔗 Links

- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### If Using Gradle Plugin

1. Update version to 1.1.0:
   ```kotlin
   plugins {
       id("com.ekino.oss.metalastic") version "1.1.0"
   }
   ```

2. Rename `className` to `registryClassName`:
   ```kotlin
   metalastic {
       metamodels {
           registryClassName = "MainMetamodels"  // was: className
       }
   }
   ```

3. Rebuild:
   ```bash
   ./gradlew clean build
   ```

### If Using Manual KSP Configuration

1. Update dependency versions:
   ```kotlin
   dependencies {
       implementation("com.ekino.oss:metalastic-core:1.1.0")
       ksp("com.ekino.oss:metalastic-processor:1.1.0")
   }
   ```

2. Update KSP argument keys:
   ```kotlin
   ksp {
       arg("metamodels.registryClassName", "MainMetamodels")  // was: metamodels.className
       arg("metamodels.main.registryClassName", "MainMetamodels")  // was: metamodels.main.className
   }
   ```

3. Rebuild:
   ```bash
   ./gradlew clean build
   ```

## 🙏 Credits

Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
