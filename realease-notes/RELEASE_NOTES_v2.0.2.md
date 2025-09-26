# Metalastic v2.0.2 Release Notes

**Release Date**: September 25, 2025
**Previous Release**: v2.0.1

This release introduces **Smart Field Name Resolution** and includes several important improvements to field type support, private class handling, and build infrastructure.

## 🌟 Major New Features

### Smart Field Name Resolution

The headline feature of v2.0.2 is **Smart Field Name Resolution** - a new intelligent system that resolves property names from `@Field(name)` annotations while respecting Kotlin naming conventions.

#### How It Works

Metalastic now follows this logic when processing `@Field(name)` annotations:

1. **Convention-compliant names** → Use annotation name as property name
2. **Non-conventional names** (snake_case, etc.) → Keep original property name
3. **Invalid identifiers** → Keep original property name

#### Examples

```java
@Document(indexName = "example")
public class ExampleDocument {
    // ✅ Uses "active" - follows camelCase convention
    @Field(type = FieldType.Boolean, name = "active")
    private boolean isActive;

    // ✅ Keeps "searchableText" - "search_content" doesn't follow camelCase
    @Field(type = FieldType.Text, name = "search_content")
    private String searchableText;

    // ✅ Keeps "numericField" - "987field" is invalid identifier
    @Field(type = FieldType.Text, name = "987field")
    private String numericField;
}
```

**Generated Code**:
```kotlin
class QExampleDocument(/* ... */) : Document<ExampleDocument>(/* ... */) {
    @JvmField
    val active: BooleanField<Boolean> = boolean("active")

    @JvmField
    val searchableText: TextField<String> = text("search_content")

    @JvmField
    val numericField: TextField<String> = text("987field")
}
```

#### Benefits

- 🎯 **Convention-compliant** - Generated properties follow Kotlin camelCase standards
- 🔄 **Dual-name system** - Annotation names used for Elasticsearch, proper names for Kotlin
- 🛡️ **Safe fallback** - Invalid or non-conventional names keep original property names
- 📝 **IDE-friendly** - Better code completion and consistency

### Enhanced KDoc Generation

Field documentation now includes the **Elasticsearch Path** for better developer experience:

```kotlin
/**
 * **Original Property:**
 * - [com.example.ExampleDocument.searchableText]
 * - Elasticsearch Type: `Text`
 * - Elasticsearch Path: `search_content`
 */
@JvmField
val searchableText: TextField<String> = text("search_content")
```

## 🚀 Other Improvements

### Full Generic Type Support

- Added `fieldType: KType` parameter to all field constructors for complete generic type modeling
- Enhanced support for complex parameterized types like `List<String>`, `Map<K,V>`
- Updated type constraint from `T : Any` to `T : Any?` to support nullable types

### Comprehensive Gradle Plugin

- **Automatic dependency management** - No more manual core/processor version coordination
- **Type-safe DSL configuration** with full IDE autocompletion
- **Multi-source-set support** including built-in shortcuts for common configurations
- **Dynamic source set configuration** for custom setups

### Enhanced Private Class Handling

- Added configurable `metalastic.generatePrivateClassMetamodels` option (default: false)
- Improved type safety for references to private classes
- Better handling of private classes in generic type arguments

## 🔧 Technical Improvements

### Build Infrastructure

- **Fixed Maven publishing issues** with proper snapshot version format
- **Improved CI/CD pipeline** with consistent version naming using `git describe`
- **Enhanced version management** with better fallback mechanisms
- **Resolved compilation errors** in CI scripts

### Code Generation

- **Enhanced field collection logic** for better annotation processing
- **Improved identifier validation** using Kotlin stdlib functions
- **Better KDoc generation** with comprehensive field information
- **Robust error handling** throughout the processing pipeline

## 📚 Documentation Updates

- **New "Smart Field Name Resolution" section** in README with comprehensive examples
- **Enhanced configuration documentation** for Gradle plugin usage
- **Updated roadmap** reflecting completed features
- **Improved examples** showing real-world usage patterns

## 🔄 Migration Guide

### From v2.0.1 to v2.0.2

**No breaking changes** - this release is fully backward compatible. However, you may notice:

1. **Property name changes**: Some generated properties may now use annotation names if they follow camelCase conventions
2. **Enhanced KDoc**: Field documentation now includes "Elasticsearch Path" information
3. **Better type support**: More accurate type modeling for generic fields

### Recommended Actions

1. **Review generated code**: Check if any property names changed due to smart resolution
2. **Update queries**: Ensure Elasticsearch queries use the correct field paths (unchanged)
3. **Enjoy improvements**: Take advantage of better IDE completion and documentation

## 📦 Artifacts

All artifacts are published to GitLab Maven Registry:

- `com.metalastic:core:2.0.2`
- `com.metalastic:processor:2.0.2`
- `com.metalastic:gradle-plugin:2.0.2`
- `com.metalastic:test:2.0.2`

## 🙏 Acknowledgments

This release includes contributions focused on improving developer experience through smarter code generation, better documentation, and enhanced build tooling. Special thanks to the comprehensive test suite that ensures reliability across all features.

---

**Full Changelog**: [v2.0.1...v2.0.2](https://gitlab.ekino.com/iperia/metalastic/-/compare/v2.0.1...v2.0.2)