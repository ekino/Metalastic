# Active Compilation Testing Patterns

This package provides reusable patterns for testing KSP processors during active compilation, ensuring tests use the same environment and API calls as production.

## Why Active Compilation Testing?

When testing KSP processors, you need access to real `Resolver`, `CodeGenerator`, and `KSPLogger` instances. The key challenge is that `ResolverImpl.instance` gets set to null in the `tearDown()` method after compilation completes. This means:

- ✅ **getSymbolsWithAnnotation() works during active compilation** (in `process()` method)
- ❌ **getSymbolsWithAnnotation() fails after compilation** (NPE from null ResolverImpl.instance)

## Testing Patterns

### Option 1: ProcessorTestBuilder (Recommended)

The most convenient approach using a fluent builder API:

```kotlin
should("test processor with builder pattern") {
  ProcessorTestBuilder.forQElasticsearchV2()
    .withSource(TestDataSources.simpleDocument())
    .test { components, processor ->
      // Test during active compilation
      val symbols = components.resolver.getSymbolsWithAnnotation("...")
      val result = processor.process(components.resolver)

      // Assertions here
      symbols.toList().shouldNotBeEmpty()
    }
}
```

**Benefits:**

- Fluent API for easy configuration
- Built-in processor factory methods
- Convenient source management
- Automatic KSP setup

### Option 2: ActiveCompilationTestBase (Flexible)

Direct access to the base testing infrastructure:

```kotlin
should("test with base class approach") {
  val testBase = object : ActiveCompilationTestBase() {
    fun execute() {
      testDuringActiveCompilation(
        sources = listOf(TestDataSources.complexDocument())
      ) { components ->
        val processor = QElasticsearchSymbolProcessorV2(
          kspLogger = components.logger,
          codeGenerator = components.codeGenerator,
          kspOptions = components.options
        )

        // Test during active compilation
        val result = processor.process(components.resolver)

        // Assertions here
      }
    }
  }

  testBase.execute()
}
```

**Benefits:**

- Maximum flexibility
- Direct control over processor creation
- Custom compilation options
- Reusable across different test scenarios

### Option 3: Custom Provider Pattern (Advanced)

For complex scenarios requiring full control over the compilation process:

```kotlin
should("test with custom provider") {
  var testResult: Result<Unit>? = null

  val testProvider = object : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return object : SymbolProcessor {
        override fun process(resolver: Resolver): List<KSAnnotated> {
          testResult = runCatching {
            // Your test logic here during active compilation
            val processor = YourProcessor(environment.logger, environment.codeGenerator)
            processor.process(resolver)

            // Assertions
          }
          return emptyList()
        }
      }
    }
  }

  // Setup and run compilation...
  testResult?.getOrThrow()
}
```

**Benefits:**

- Complete control over compilation lifecycle
- Custom processor provider logic
- Integration with existing test infrastructure
- Advanced debugging capabilities

## Component Classes

### ActiveCompilationTestBase

Base class providing core functionality:

- `testDuringActiveCompilation()` - Main testing method
- `createCompilation()` - Compilation setup
- `defaultSources()` - Standard test sources
- `defaultOptions()` - Standard KSP options

### ProcessorTestBuilder

Fluent builder for common scenarios:

- `withSource()` / `withSources()` - Add source files
- `withOption()` / `withOptions()` - Configure KSP options
- `withProcessor()` - Custom processor factory
- `withQElasticsearchV2()` - Pre-configured for QElasticsearch
- `test()` - Execute the test

### TestDataSources

Pre-built source files for common scenarios:

- `simpleDocument()` - Basic @Document with keyword/text fields
- `allTypesDocument()` - All Elasticsearch field types
- `nestedDocument()` - Object and nested field structures
- `multiFieldDocument()` - MultiField annotations
- `complexDocument()` - Multi-level nested structures
- `javaCompatDocument()` - Java-compatible types

### KspTestComponents

Data class holding captured KSP components:

```kotlin
data class KspTestComponents(
  val logger: KSPLogger,
  val codeGenerator: CodeGenerator,
  val resolver: Resolver,
  val options: Map<String, String>
)
```

## Usage Examples

### Simple Test

```kotlin
ProcessorTestBuilder.forQElasticsearchV2()
  .withSource(TestDataSources.simpleDocument())
  .test { components, processor ->
    val result = processor.process(components.resolver)
    result.size shouldBe 0 // No deferred symbols
  }
```

### Custom Options

```kotlin
ProcessorTestBuilder.forQElasticsearchV2()
  .withOptions(mapOf(
    "qelasticsearch.generateJavaCompatibility" to "false"
  ))
  .withSource(TestDataSources.allTypesDocument())
  .test { components, processor ->
    // Test with custom options
  }
```

### Multiple Sources

```kotlin
ProcessorTestBuilder.forQElasticsearchV2()
  .withSources(
    TestDataSources.simpleDocument(),
    TestDataSources.nestedDocument(),
    TestDataSources.multiFieldDocument()
  )
  .test { components, processor ->
    val symbols = components.resolver.getSymbolsWithAnnotation(
      "org.springframework.data.elasticsearch.annotations.Document"
    )
    symbols.toList().size shouldBe 3
  }
```

### Custom Source

```kotlin
ProcessorTestBuilder.forQElasticsearchV2()
  .withKotlinSource("Custom.kt", """
    @Document(indexName = "custom")
    data class CustomDoc(@Field val id: String)
  """.trimIndent())
  .test { components, processor ->
    // Test with inline source
  }
```

## Best Practices

1. **Use ProcessorTestBuilder for most scenarios** - It handles the complexity for you
2. **Test during active compilation** - All testing logic must be in the test block
3. **Use TestDataSources** - Reuse common source patterns
4. **Verify getSymbolsWithAnnotation()** - Ensure it finds expected symbols
5. **Check processor results** - Verify deferred symbols and generated code
6. **Avoid post-compilation testing** - ResolverImpl.instance will be null

## Migration from Legacy Approach

Old approach (doesn't work reliably):

```kotlin
// ❌ This fails with NPE after compilation
val compilation = KotlinCompilation().apply { /* setup */ }
compilation.compile()
// getSymbolsWithAnnotation() fails here
```

New approach (works reliably):

```kotlin
// ✅ This works during active compilation
ProcessorTestBuilder.forQElasticsearchV2()
  .test { components, processor ->
    // getSymbolsWithAnnotation() works here
    val symbols = components.resolver.getSymbolsWithAnnotation("...")
  }
```

## Technical Details

The patterns work by:

1. Creating a test `SymbolProcessorProvider`
2. Capturing real KSP components in `create()`
3. Running test logic in the `process()` method during active compilation
4. Ensuring `ResolverImpl.instance` is properly initialized
5. Providing the same environment as production

This ensures tests use identical API calls and components as the real processor.
