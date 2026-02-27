# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.2] - 2026-02-27

### Changed

- **Dependencies:** Kotlin 2.3.0 → 2.3.10
- **Dependencies:** KSP 2.3.4 → 2.3.6
- **Dependencies:** Gradle Wrapper 9.3.0 → 9.3.1
- **Dependencies:** detekt 2.0.0-alpha.1 → 2.0.0-alpha.2
- **Dependencies:** Spotless 8.1.0 → 8.2.1
- **Dependencies:** MockK 1.14.7 → 1.14.9
- **Dependencies:** JUnit Jupiter 6.0.2 → 6.0.3
- **Dependencies:** kotlin-logging 7.0.14 → 8.0.01

## [1.2.0] - 2025-12-22

### Changed

- **Dependencies:** Kotlin 2.2.20 → 2.3.0
- **Dependencies:** KSP 2.2.20-2.0.3 → 2.3.4 (new independent versioning)
- **Dependencies:** Spring Data Elasticsearch 6.0.0 → 6.0.1
- **Dependencies:** detekt 1.23.8 → 2.0.0-alpha.1
  - Plugin ID changed from `io.gitlab.arturbosch.detekt` to `dev.detekt`
- **Documentation:** Updated all version references to 1.2.0

## [1.1.0] - 2025-11-24

### Breaking Changes

#### Gradle Plugin API

- **BREAKING:** Renamed `className` property to `registryClassName` in Gradle plugin DSL for clarity
  - `metamodels.className` → `metamodels.registryClassName`
  - Applies to global and source-set specific configuration
  - **Migration:** Simply rename the property in your `build.gradle.kts`

#### KSP Argument Keys

- **BREAKING:** Updated KSP argument keys for manual configuration
  - `metamodels.className` → `metamodels.registryClassName`
  - `metamodels.{sourceSet}.className` → `metamodels.{sourceSet}.registryClassName`
  - **Migration:** Update KSP args if using manual configuration (not Gradle plugin)

### Added

- **Documentation:** Comprehensive configuration guide improvements
  - Added "Understanding What Gets Generated" section
  - Enhanced property documentation with detailed explanations
  - Source set configuration guide
- **Documentation:** Centralized version management using VitePress data loading
  - Single source of truth for all version numbers
  - Easier documentation maintenance
- **Documentation:** Enhanced Query DSL guide
  - Complete StartBound mathematical notation documentation
  - Kotlin range operators (`..` and `..<`) with `fromInclusive()`/`fromExclusive()`
  - Unbounded ranges with null
  - DSL syntax comparison (operator vs classical)
  - Built-in `bool { }` and `nested { }` functions
  - Safety validation and logging documentation
- **Documentation:** New "Understanding Metamodels" guide
  - Core concepts and architecture
  - Type system explanation
  - Generation process details
- **Documentation:** Enhanced Field Types Reference
  - Spring FieldType → Metalastic mapping table
  - Organized by Elasticsearch field categories
- **Documentation:** Simplified README.md (1,689 → 253 lines)
  - Focused landing page directing to docs site
  - Prominent link to documentation site

### Changed

- **Code Quality:** Composed constants in CoreConstants from base components
  - Eliminates string duplication
  - Single source of truth for KSP argument key structure
- **Documentation:** Removed QueryDSL references throughout
  - Use "Models" prefix as alternative example instead
- **Documentation:** Updated all version references to 1.1.0

### Fixed

- **Documentation:** Fixed understanding-metamodels.md example to match actual generated code
  - Removed default value from `fieldType` parameter
  - Added `INDEX_NAME` constant
  - Show explicit `fieldType` in companion object
- **Documentation:** Fixed MultiField documentation to match actual generated code structure

## [1.0.0] - 2025-11-12
### Added
- **metalastic-core**: Core library
- **metalastic-processor**: Annotation processor for compile-time code generation
- **metalastic-gradle-plugin**: Gradle plugin for build integration
- **elasticsearch-dsl-5.0 through 5.5**: DSL modules for Elasticsearch versions 5.0 to 5.5
    - Query builders (boolean, term, match, range)
    - Aggregation support
    - Type-safe DSL API

[Unreleased]: https://github.com/ekino/Metalastic/compare/v1.2.2...HEAD
[1.2.2]: https://github.com/ekino/Metalastic/compare/v1.2.1...v1.2.2
[1.2.0]: https://github.com/ekino/Metalastic/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/ekino/Metalastic/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/ekino/Metalastic/releases/tag/v1.0.0