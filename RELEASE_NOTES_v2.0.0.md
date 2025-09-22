# QElasticsearch v2.0.0 Release Notes

🎉 **Major Release: V2 Processor Architecture**

We're excited to announce QElasticsearch v2.0.0, featuring a completely redesigned annotation processor architecture that delivers enhanced performance, improved code generation, and better developer experience.

## 🚀 Major Features

### Complete V2 Processor Architecture Redesign
- **Enhanced Collecting Phase**: Completely rewritten annotation processor with a modern, maintainable architecture
- **Unified Model Hierarchy**: New `QElasticsearchModel` sealed class hierarchy for better type safety
- **2-Step Collection Approach**:
  - Discovery phase for initial document scanning
  - Recursive exploration phase with cycle detection
- **Performance Improvements**: Significantly faster code generation and better memory usage

### Advanced Generic Type Support
- **Synthetic KSP Implementation**: Complete synthetic KSP symbol implementations for proper generic handling
- **Type Parameter Support**: Full support for complex generic scenarios including bounded type parameters
- **Star Projection**: Advanced star projection handling for complex nested generics
- **Type Argument Resolution**: Proper type argument substitution and replacement

### Enhanced Field Collection
- **FieldCollector Redesign**: New field collection system with `originalName` property support
- **Recursive Type Explorer**: Intelligent type exploration with built-in cycle detection
- **Inner Document Resolution**: Support for inner `@Document` class resolution

## 🔧 Infrastructure Improvements

### Build System Upgrades
- **Gradle 9.1.0**: Upgraded from Gradle 8.14 to latest stable version
- **Git-Based Versioning**: Automatic version management using Git tags (`git describe`)
- **CI/CD Enhancements**: Improved CI pipeline with Amazon Corretto Alpine and npm support

### Dependencies & Environment
- **Amazon Corretto**: Switched from OpenJDK to Amazon Corretto 21 Alpine for better performance
- **Node.js Integration**: Added npm support for markdown formatting in CI
- **Enhanced Toolchain**: Improved development environment with better tooling support

## 🏗️ Architecture Enhancements

### Code Organization
- **Modular Design**: Better separation of concerns with dedicated packages:
  - `collecting/`: Field and graph collection logic
  - `building/`: Code generation and metamodel building
  - `kspimplementation/`: Synthetic KSP symbol implementations
  - `options/`: Configuration and processor options
  - `report/`: Enhanced reporting and logging

### Code Generation Improvements
- **KotlinPoet Extensions**: Enhanced code generation utilities
- **Field Type Mappings**: Improved field type to Kotlin type mapping system
- **Import Context**: Smart import management and optimization
- **File Writing**: Dedicated file writing system with better error handling

## 🧪 Testing Framework
- **Comprehensive Test Suite**: New testing framework with `ProcessorTestBuilder`
- **KSP Test Utilities**: Dedicated utilities for testing annotation processors
- **Test Data Sources**: Extensive test datasets covering various scenarios
- **Java Compatibility**: Enhanced Java interoperability testing

## 📚 Documentation & Guidelines
- **Logo Design Guidelines**: Complete branding guidelines for "Metalastic" concept
- **Enhanced Documentation**: Improved README and comprehensive testing documentation
- **Code Examples**: Real-world usage examples and integration patterns

## 🐛 Bug Fixes & Improvements
- **Metamodels Naming**: Fixed naming conflicts in metamodels generation
- **Package Resolution**: Improved package naming and import resolution
- **Code Quality**: Reduced cyclomatic complexity and improved maintainability
- **Publishing**: Fixed module exclusions and publishing configuration

## 💔 Breaking Changes
- **Processor Architecture**: Complete rewrite of the annotation processor (internal change)
- **Field API**: Some internal field APIs have changed (should not affect end users)
- **Package Structure**: Reorganized internal packages (annotation processor only)

## 🔗 Migration Guide
For most users, this should be a drop-in replacement. The generated Q-classes maintain the same API. If you're extending the processor or using internal APIs, please refer to the updated documentation.

## 🚀 What's Next
- Configurable Q-prefix through KSP options
- Additional field type support
- Performance optimizations
- Enhanced IDE integration

---

**Full Changelog**: Compare [v1.0.0...v2.0.0](https://gitlab.ekino.com/iperia/qelasticsearch/-/compare/v1.0.0...master)

**Installation**:
```kotlin
dependencies {
    implementation("com.qelasticsearch:core:2.0.0")
    ksp("com.qelasticsearch:processor:2.0.0")
}
```

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>