# Metalastic Annotation Processor Architecture

This document provides a comprehensive overview of the Metalastic annotation processor module, which generates type-safe DSL classes from Spring Data Elasticsearch `@Document` annotated classes at compile time.

## Overview

The processor is built on **Kotlin Symbol Processing (KSP)** and uses **KotlinPoet** for code generation. It transforms existing Spring Data Elasticsearch domain classes into type-safe query DSL objects automatically.

## Module Structure

```mermaid
graph TB
    subgraph "Processor Module"
        A[MetalasticSymbolProcessor] --> B[NestedClassProcessor]
        A --> C[FieldGenerators]
        A --> D[ObjectFieldRegistry]
        A --> E[FieldTypeExtractor]
        
        B --> F[CodeGenerationUtils]
        C --> F
        D --> F
        E --> F
        
        G[SharedTypes] --> A
        H[FieldTypeMappingBuilder] --> A
        I[MetalasticSymbolProcessorProvider] --> A
    end
    
    subgraph "External Dependencies"
        J[KSP API]
        K[KotlinPoet]
        L[Spring Data Elasticsearch]
    end
    
    A --> J
    A --> K
    A --> L
```

## Core Components

### 1. MetalasticSymbolProcessor
**Main orchestrator** that coordinates the entire code generation process.

**Key Responsibilities:**
- Discovers `@Document` annotated classes
- Orchestrates object field collection
- Generates Q-classes for documents and nested objects
- Manages file output and deduplication

### 2. NestedClassProcessor
**Dependency analyzer** that recursively discovers all object fields that need Q-class generation.

**Key Responsibilities:**
- Collects nested classes within document classes
- Processes properties to find referenced object types
- Maintains global registry of object fields
- Handles recursive object field discovery

### 3. FieldGenerators
**Field property generator** that creates type-safe field definitions for Q-classes.

**Key Responsibilities:**
- Generates simple field properties (text, keyword, numeric, etc.)
- Handles multi-field properties with inner fields
- Creates object and nested field properties
- Manages KDoc documentation generation

### 4. ObjectFieldRegistry
**Object field manager** that handles generation of object field properties and their type resolution.

**Key Responsibilities:**
- Generates object and nested field properties
- Resolves object field types and classes
- Manages constructor calls for object and nested fields

### 5. FieldTypeExtractor
**Type analyzer** that determines Elasticsearch field types and validates object references.

**Key Responsibilities:**
- Extracts field types from `@Field` annotations
- Identifies object and nested field types
- Validates collection element types
- Handles complex type analysis

## Processing Flow

```mermaid
sequenceDiagram
    participant KSP as KSP Runtime
    participant Main as MetalasticSymbolProcessor
    participant Nested as NestedClassProcessor
    participant FieldExt as FieldTypeExtractor
    participant FieldGen as FieldGenerators
    participant ObjReg as ObjectFieldRegistry
    participant Poet as KotlinPoet

    KSP->>Main: process(resolver)
    Main->>Main: Find @Document classes
    
    loop For each document class
        Main->>Nested: collectObjectFields(documentClass)
        Nested->>Nested: Collect direct nested classes
        Nested->>FieldExt: Process properties for references
        Nested->>Nested: Register object fields (recursive)
    end
    
    Main->>Main: Get global object fields
    Main->>+ObjReg: Create ObjectFieldRegistry
    
    loop For each document class
        Main->>Main: processDocumentClass()
        Main->>FieldGen: Process properties
        Main->>ObjReg: Generate object fields
        Main->>Poet: Generate Q-class file
    end
    
    loop For each standalone object field
        Main->>Main: generateAllObjectFields()
        Main->>FieldGen: Process object properties
        Main->>Poet: Generate object field class
    end
    
    Main->>KSP: Return empty list (success)
```

## Data Flow Architecture

```mermaid
graph TD
    subgraph "Input Phase"
        A[Spring Data Classes] --> B[@Document Classes]
        B --> C[KSP Resolver]
    end
    
    subgraph "Analysis Phase"
        C --> D[Document Discovery]
        D --> E[Object Field Collection]
        E --> F[Type Analysis]
        F --> G[Dependency Resolution]
    end
    
    subgraph "Generation Phase"  
        G --> H[Field Type Mapping]
        H --> I[Property Generation]
        I --> J[Class Construction]
        J --> K[File Generation]
    end
    
    subgraph "Output Phase"
        K --> L[Q-Document Classes]
        K --> M[Q-Object Field Classes]
        L --> N[Generated Kotlin Files]
        M --> N
    end
    
    style A fill:#e1f5fe
    style N fill:#c8e6c9
```

## Key Data Structures

### ImportContext
Manages import statements and optimal type names during code generation:

```kotlin
class ImportContext(
  private val currentPackage: String,
  val usedImports: MutableSet<String> = mutableSetOf(),
)
```

### ProcessedFieldType
Represents analyzed field type information:

```kotlin
data class ProcessedFieldType(
  val elasticsearchType: FieldType,      // Elasticsearch field type
  val kotlinType: KSTypeReference,       // Kotlin type reference
  val kotlinTypeName: String,            // Simple type name
  val isObjectType: Boolean,             // Whether it needs object field handling
)
```

### ObjectFieldInfo
Contains metadata about object fields that need Q-class generation:

```kotlin
data class ObjectFieldInfo(
  val className: String,                 // Generated Q-class name
  val packageName: String,               // Package for generated class
  val classDeclaration: KSClassDeclaration, // Original class declaration
  val qualifiedName: String,             // Fully qualified original class name
  val parentDocumentClass: KSClassDeclaration? = null, // Parent document if any
)
```

## Object Field Collection Strategy

```mermaid
graph TD
    A[Document Class] --> B[Collect Direct Nested Classes]
    A --> C[Process Document Properties]
    
    B --> D[Register Nested Classes]
    C --> E[Find Referenced Object Types]
    
    E --> F[Register Referenced Objects]
    F --> G[Recursive Object Field Collection]
    
    D --> H[Process Nested Class Properties]
    G --> H
    H --> I[Find Transitive References]
    I --> J[Register All Object Fields]
    
    J --> K[Global Object Field Registry]
    
    style A fill:#ffeb3b
    style K fill:#4caf50
```

**Strategy Benefits:**
- ✅ **Targeted Processing** - Only processes classes actually referenced by documents
- ✅ **Recursive Discovery** - Finds deep nested object references
- ✅ **Deduplication** - Prevents generating duplicate classes
- ✅ **Efficient** - No full classpath scanning required

## Field Type Mapping System

```mermaid
graph LR
    subgraph "Elasticsearch Types"
        A[Text] --> B["TextField&lt;String&gt;"]
        C[Keyword] --> D["KeywordField&lt;String&gt;"]
        E[Long] --> F["LongField&lt;Long&gt;"]
        G[Boolean] --> H["BooleanField&lt;Boolean&gt;"]
        I[Object] --> J[ObjectField]
        K[Nested] --> L[NestedField]
    end
    
    subgraph "Multi-Field Handling"
        M[@MultiField] --> N[CustomMultiField]
        N --> O[MainField + InnerFields]
    end
    
    subgraph "Generated DSL"
        B --> P["textField&lt;String&gt;(\"field\")"]
        D --> Q["keywordField&lt;String&gt;(\"field\")"]
        F --> R["longField&lt;Long&gt;(\"field\")"]
        H --> S["booleanField&lt;Boolean&gt;(\"field\")"]
        J --> T["ObjectConstructor(this, \"field\", false)"]
        L --> U["ObjectConstructor(this, \"field\", true)"]
        O --> V["MultiFieldConstructor(this, \"field\")"]
    end
    
    style A fill:#e3f2fd
    style P fill:#c8e6c9
```

## Code Generation Process

### 1. Document Class Generation

```mermaid
sequenceDiagram
    participant Main as Main Processor
    participant Builder as TypeSpec.Builder
    participant FieldGen as FieldGenerators
    participant Poet as KotlinPoet

    Main->>Builder: Create data object QDocument
    Main->>Builder: Add superclass Index("indexName")
    Main->>Builder: Add generated annotation
    
    loop For each property
        Main->>FieldGen: processProperty()
        FieldGen->>FieldGen: Determine field type
        FieldGen->>FieldGen: Generate property + KDoc
        FieldGen->>Builder: Add property to class
    end
    
    loop For each nested object
        Main->>Builder: Add nested object class
    end
    
    Main->>Poet: FileSpec.builder()
    Poet->>Poet: Add imports and formatting
    Main->>Poet: Write generated file
```

### 2. Object Field Class Generation

```mermaid
sequenceDiagram
    participant Main as Main Processor
    participant Builder as TypeSpec.Builder
    participant ObjReg as ObjectFieldRegistry

    Main->>Builder: Create class QObjectField
    Main->>Builder: Add constructor(parent, path, nested)
    Main->>Builder: Add superclass ObjectField
    
    loop For each object property
        Main->>ObjReg: generateObjectFieldProperty()
        ObjReg->>ObjReg: Determine delegation type
        ObjReg->>Builder: Add property with delegate
    end
    
    Main->>Builder: Add KDoc documentation
    Main->>Main: Write object field file
```

## Import Management System

The Metalastic processor uses a sophisticated **import optimization system** with package proximity prioritization to ensure generated files have clean, optimal imports while avoiding conflicts. **KotlinPoet automatically handles external type imports**, while the processor manages core DSL type imports.

### Import Management Architecture

```mermaid
graph TD
    subgraph "Import Collection Phase"
        A[Code Generation] --> B[ImportContext]
        B --> C[Type Usage Registration]
        B --> D[Package Proximity Analysis]
        E[KotlinPoet] --> F[Automatic Type Import Management]
    end
    
    subgraph "Import Optimization"
        C --> G[Conflict Detection]
        D --> H[Priority Calculation]
        F --> I[External Type References]
    end
    
    subgraph "Import Application Phase"
        G --> J[Core DSL Imports]
        H --> K[Optimal Type Names]
        I --> L[Automatic Qualified Imports]
    end
    
    subgraph "Generated Output"
        J --> M[Final Import Statements]
        K --> M
        L --> M
        M --> N[Clean Generated File]
    end
    
    style B fill:#ffeb3b
    style F fill:#4caf50
    style M fill:#c8e6c9
```

### Import Optimization with Package Proximity + KotlinPoet Automatic Management

**1. Core DSL Type Management**
```kotlin
// Type usage registration during code generation
importContext.registerTypeUsage("com.metalastic.core.TextField")
importContext.registerTypeUsage("com.example.domain.User")
importContext.registerTypeUsage("com.example.other.User") // Conflict!

// Package proximity prioritization resolves conflicts:
// - Same package types win over cross-package
// - Closer packages win over distant ones
// - Locally defined nested classes use simple names
```

**2. Conflict Resolution and Optimal Naming**
```kotlin
// After finalization, optimal type names are determined:
importContext.getOptimalTypeName("com.metalastic.core.TextField") // -> "TextField" (imported)
importContext.getOptimalTypeName("com.example.domain.User") // -> "User" (priority winner, imported)  
importContext.getOptimalTypeName("com.example.other.User") // -> "com.example.other.User" (fully qualified)
```

**3. External Type Imports (Automatic via KotlinPoet)**
```kotlin
// KotlinPoet automatically handles external type imports:
// - External domain classes (com.example.domain.User)
// - Standard annotations (jakarta.annotation.Generated)
// - Generic type arguments (ParametrizedType<String>)
// - Collections (MutableList<String>, MutableMap<String, Int>)

// Results in automatically generated imports:
import com.example.domain.User
import jakarta.annotation.Generated
import kotlin.collections.MutableList
```

### Import Collection Flow

```mermaid
sequenceDiagram
    participant FG as FieldGenerators
    participant ORG as ObjectFieldRegistry
    participant IC as ImportContext
    participant Utils as CodeGenerationUtils
    participant FB as FileBuilder

    Note over FG,ORG: During Code Generation
    
    FG->>IC: registerTypeUsage("TextField")
    FG->>Utils: generateFieldProperty()
    Note over Utils: KotlinPoet handles type imports automatically
    
    ORG->>IC: registerTypeUsage("QUserAddress")
    ORG->>IC: generateObjectFieldProperty()
    
    Note over IC: Conflict Detection & Resolution
    
    IC->>IC: finalizeImportDecisions()
    IC->>FB: addImportsToFileBuilder()
    FB->>FB: Generate optimal import statements
    FB->>FB: Create final file with clean imports
```

### Smart Type Name Processing

The processor intelligently processes type names while collecting necessary imports:

```kotlin
fun extractImportsAndSimplifyTypeName(typeName: TypeName, usedImports: MutableSet<String>): String =
  when (typeName) {
    is ClassName -> {
      // Only import non-standard library types
      if (
        typeName.packageName.isNotEmpty() &&
          !typeName.packageName.startsWith("kotlin") &&
          !typeName.packageName.startsWith("java.lang")
      ) {
        usedImports.add("${typeName.packageName}.${typeName.simpleName}")
      }
      typeName.simpleName  // Return simple name for code generation
    }
    
    is ParameterizedTypeName -> {
      // Recursively process generic types
      val baseSimpleName = extractImportsAndSimplifyTypeName(typeName.rawType, usedImports)
      val typeArgs = typeName.typeArguments.joinToString(", ") { arg ->
        extractImportsAndSimplifyTypeName(arg, usedImports)
      }
      "$baseSimpleName&lt;$typeArgs&gt;"
    }
    
    else -> typeName.toString()
  }
```

### Import Application Implementation

```kotlin
private fun addImportsToFileBuilder(fileBuilder: FileSpec.Builder, importContext: ImportContext) {
  // Add optimized imports based on conflict resolution
  importContext.usedImports.forEach { qualifiedName ->
    when {
      qualifiedName.contains(".Companion.") -> addCompanionImport(fileBuilder, qualifiedName)
      qualifiedName.startsWith(CoreConstants.CORE_PACKAGE) -> addCorePackageImport(fileBuilder, qualifiedName)
      else -> addDefaultCoreImport(fileBuilder, qualifiedName)
    }
  }

  // External type imports are handled automatically by KotlinPoet
  // No manual import management needed for:
  // - External domain classes (com.example.User) 
  // - Generic type arguments (ParametrizedType<String>)
  // - Standard library types (MutableList, MutableMap)
  // - Jakarta annotations (@Generated)
}
```

### Generated Import Examples

**Simple Document:**
```kotlin
// Generated imports for basic document  
import com.metalastic.core.Index
import com.metalastic.core.KeywordField
import com.metalastic.core.ObjectField
import com.metalastic.core.TextField
import jakarta.annotation.Generated
import kotlin.String
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
```

**Complex Document with Nested Objects:**
```kotlin
// Generated imports for complex document with conflict resolution
import com.metalastic.core.Index
import com.metalastic.core.MultiField
import com.metalastic.core.ObjectField
import com.metalastic.core.TextField
import com.example.domain.Category  // Automatic via KotlinPoet
import com.example.domain.Review     // Automatic via KotlinPoet
import jakarta.annotation.Generated
import kotlin.Boolean
import kotlin.String
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
```

### Import Management Benefits

- ✅ **Conflict Resolution** - Automatically resolves name conflicts with package proximity prioritization
- ✅ **Smart Filtering** - KotlinPoet excludes unnecessary imports automatically  
- ✅ **Organized Structure** - Clean import organization with optimal type name usage
- ✅ **Performance Optimized** - KotlinPoet handles complex type analysis efficiently
- ✅ **Clean Generated Code** - No unused imports, consistent ordering, no redundant qualifiers
- ✅ **Nested Class Optimization** - Locally defined nested classes use simple names without imports

## Utility Functions Architecture

The processor uses top-level utility functions for common operations:

```mermaid
graph TD
    subgraph "CodeGenerationUtils.kt"
        A[isCollectionType] --> F[Type Checking]
        B[isStandardLibraryType] --> F
        C[getSimpleTypeName] --> G[Type Name Extraction]
        D[getCollectionElementType] --> G
        E[createKotlinPoetTypeName] --> H[KotlinPoet Integration]
        I[extractImportsAndSimplifyTypeName] --> H
        J[extractFieldTypeFromAnnotation] --> K[Annotation Processing]
        L[generateFieldKDoc] --> M[Documentation Generation]
    end
    
    subgraph "Usage"
        N[NestedClassProcessor] --> F
        N --> G
        O[FieldGenerators] --> F
        O --> H
        O --> K
        O --> M
        P[FieldTypeExtractor] --> F
        P --> G
        P --> K
        Q[ObjectFieldRegistry] --> G
        Q --> M
    end
    
    style A fill:#fff3e0
    style F fill:#e8f5e8
```

## Generated Code Structure

### Document Q-Class Example

```kotlin
@Generated("com.metalastic.processor.MetalasticSymbolProcessor")
data object QUser : Index("users") {
    /**
     * Elasticsearch field for property [com.example.User.id].
     * - Elasticsearch Type: `Keyword`
     */
    @JvmField
    val id: KeywordField<String> = keywordField<String>("id")
    
    /**
     * Elasticsearch field for property [com.example.User.profile].
     * - Elasticsearch Type: `Object`
     */
    @JvmField
    val profile: Profile = Profile(this, "profile", false)
    
    // Nested object class included inline
    class Profile(
        parent: ObjectField?, 
        path: String, 
        nested: Boolean
    ) : ObjectField(parent, path, nested) {
        @JvmField
        val firstName: TextField<String> = textField<String>("firstName")
        @JvmField
        val lastName: TextField<String> = textField<String>("lastName")
    }
}
```

### Multi-Field Example

```kotlin
@JvmField
val name: NameMultiField = NameMultiField(this, "name")

class NameMultiField(
    parent: ObjectField,
    path: String,
) : MultiField<TextField<String>>(parent, TextField(parent, path)) {
    @JvmField
    val keyword: KeywordField<String> = keywordField<String>("keyword")
    @JvmField
    val analyzed: TextField<String> = textField<String>("analyzed")
}
```

## Performance Optimizations

### Before Optimization
```mermaid
graph TD
    A[KSP Process Start] --> B[Scan ENTIRE Classpath]
    B --> C[Process 15,000+ Classes]
    C --> D[Filter Relevant Classes]
    D --> E[Generate Q-Classes]
    E --> F[~30 seconds compilation]
    
    style B fill:#ffcdd2
    style C fill:#ffcdd2
    style F fill:#ffcdd2
```

### After Optimization  
```mermaid
graph TD
    A[KSP Process Start] --> B[Find @Document Classes]
    B --> C[Recursive Object Discovery]
    C --> D[Process ~10-50 Classes]
    D --> E[Generate Q-Classes]
    E --> F[~2 seconds compilation]
    
    style B fill:#c8e6c9
    style C fill:#c8e6c9
    style D fill:#c8e6c9
    style F fill:#c8e6c9
```

**Key Optimizations:**
- ✅ **Targeted Class Discovery** - Only process referenced classes
- ✅ **Top-level Utility Functions** - No object instantiation overhead
- ✅ **Efficient Dependency Tracking** - Recursive but bounded collection
- ✅ **Incremental Compilation** - KSP handles change detection

## Error Handling & Logging

```mermaid
graph TD
    A[Processing Start] --> B{Document Classes Found?}
    B -->|No| C[Return Empty - No Generation]
    B -->|Yes| D[Begin Processing]
    
    D --> E{Error During Processing?}
    E -->|Yes| F[Log Error + Exception]
    E -->|No| G[Continue Processing]
    
    F --> H[Return Empty - Graceful Failure]
    G --> I[Generate Files]
    I --> J[Log Success Information]
    
    style C fill:#fff3e0
    style F fill:#ffcdd2
    style H fill:#ffcdd2
    style I fill:#c8e6c9
    style J fill:#c8e6c9
```

**Error Handling Strategy:**
- ✅ **Graceful Degradation** - Compilation continues even if processor fails
- ✅ **Detailed Logging** - KSPLogger provides detailed error information
- ✅ **Exception Containment** - runCatching prevents processor crashes
- ✅ **User-Friendly Messages** - Clear logging for debugging

## Integration Points

### KSP Integration
- **SymbolProcessorProvider** - Entry point for KSP runtime
- **Resolver** - Provides access to compilation symbols
- **CodeGenerator** - Handles file creation and output
- **Dependencies** - Manages incremental compilation tracking

### KotlinPoet Integration
- **FileSpec** - Represents generated Kotlin files
- **TypeSpec** - Represents classes and objects
- **PropertySpec** - Represents class properties
- **AnnotationSpec** - Handles annotation generation

### Spring Data Elasticsearch Integration
- **@Document** - Primary trigger annotation for processing
- **@Field** - Field type and configuration source
- **@MultiField** - Multi-field configuration support
- **FieldType** - Elasticsearch field type enumeration

## Future Enhancements

```mermaid
graph TD
    subgraph "Current Capabilities"
        A[Basic Field Types]
        B[Object/Nested Fields]
        C[Multi-Field Support]
        D[Path Traversal]
    end
    
    subgraph "Planned Enhancements"
        E[Query Builder Integration] --> F[Direct ES Query Generation]
        G[Aggregation DSL] --> H[Type-Safe Aggregations]
        I[Query Validation] --> J[Compile-time Query Checking]
        K[Performance Metrics] --> L[Query Performance Insights]
    end
    
    A --> E
    B --> G
    C --> I
    D --> K
    
    style E fill:#e1f5fe
    style G fill:#e1f5fe
    style I fill:#e1f5fe
    style K fill:#e1f5fe
```

## Conclusion

The Metalastic annotation processor represents a sophisticated code generation system that:

- ✅ **Efficiently processes** only the classes that matter
- ✅ **Generates type-safe** DSL code with full IDE support  
- ✅ **Handles complex scenarios** like nested objects and multi-fields
- ✅ **Provides excellent performance** through targeted processing
- ✅ **Maintains compatibility** with existing Spring Data Elasticsearch code
- ✅ **Offers extensibility** for future enhancements

The architecture is designed for **performance**, **maintainability**, and **extensibility**, making it a solid foundation for type-safe Elasticsearch query building in Kotlin.