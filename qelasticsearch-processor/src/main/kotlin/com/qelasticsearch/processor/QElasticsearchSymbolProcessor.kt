package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.MultiField

/**
 * Context for import management during code generation.
 */
private data class ImportContext(
    val usedImports: MutableSet<String> = mutableSetOf(),
    val typeImports: MutableSet<String> = mutableSetOf(),
)

/**
 * Field type mapping for generating DSL methods and classes.
 */
private data class FieldTypeMapping(
    val delegate: String,
    val className: String,
)

/**
 * Constants for DSL generation.
 */
private object DSLConstants {
    const val DSL_PACKAGE = "com.qelasticsearch.dsl"
    const val Q_PREFIX = "Q"
    const val INDEX_CLASS = "Index"
    const val OBJECT_FIELDS_CLASS = "ObjectFields"
    const val MULTI_FIELD_PROXY = "MultiFieldProxy"
}

/**
 * KSP-based annotation processor that generates type-safe Elasticsearch DSL classes
 * from Spring Data Elasticsearch @Document annotated classes.
 */
class QElasticsearchSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    /**
     * Unified mapping of FieldType to DSL method and class names.
     * Eliminates duplication between getFieldDelegate() and getFieldClass().
     * Uses runtime detection to support different Spring Data Elasticsearch versions.
     */
    private val fieldTypeMappings: Map<FieldType, FieldTypeMapping> by lazy {
        buildFieldTypeMappings()
    }

    /**
     * Builds field type mappings with runtime detection for version compatibility.
     * Only includes FieldType enum values that exist in the current Spring Data Elasticsearch version.
     */
    private fun buildFieldTypeMappings(): Map<FieldType, FieldTypeMapping> {
        val mappings = mutableMapOf<FieldType, FieldTypeMapping>()

        // Core field types - available in all versions
        safeAddMapping(mappings, "Text", "text()", "TextField")
        safeAddMapping(mappings, "Keyword", "keyword()", "KeywordField")
        safeAddMapping(mappings, "Binary", "binary()", "BinaryField")

        // Numeric field types - available in all versions
        safeAddMapping(mappings, "Long", "long()", "LongField")
        safeAddMapping(mappings, "Integer", "integer()", "IntegerField")
        safeAddMapping(mappings, "Short", "short()", "ShortField")
        safeAddMapping(mappings, "Byte", "byte()", "ByteField")
        safeAddMapping(mappings, "Double", "double()", "DoubleField")
        safeAddMapping(mappings, "Float", "float()", "FloatField")
        safeAddMapping(mappings, "Half_Float", "halfFloat()", "HalfFloatField")
        safeAddMapping(mappings, "Scaled_Float", "scaledFloat()", "ScaledFloatField")

        // Date/time field types
        safeAddMapping(mappings, "Date", "date()", "DateField")
        safeAddMapping(mappings, "Date_Nanos", "dateNanos()", "DateNanosField")

        // Boolean field type
        safeAddMapping(mappings, "Boolean", "boolean()", "BooleanField")

        // Range field types
        safeAddMapping(mappings, "Integer_Range", "integerRange()", "IntegerRangeField")
        safeAddMapping(mappings, "Float_Range", "floatRange()", "FloatRangeField")
        safeAddMapping(mappings, "Long_Range", "longRange()", "LongRangeField")
        safeAddMapping(mappings, "Double_Range", "doubleRange()", "DoubleRangeField")
        safeAddMapping(mappings, "Date_Range", "dateRange()", "DateRangeField")
        safeAddMapping(mappings, "Ip_Range", "ipRange()", "IpRangeField")

        // Specialized field types
        safeAddMapping(mappings, "Object", "objectField()", "ObjectField")
        safeAddMapping(mappings, "Nested", "nestedField()", "NestedField")
        safeAddMapping(mappings, "Ip", "ip()", "IpField")
        safeAddMapping(mappings, "TokenCount", "tokenCount()", "TokenCountField")
        safeAddMapping(mappings, "Percolator", "percolator()", "PercolatorField")
        safeAddMapping(mappings, "Flattened", "flattened()", "FlattenedField")
        safeAddMapping(mappings, "Search_As_You_Type", "searchAsYouType()", "SearchAsYouTypeField")

        // Geo types
        safeAddMapping(mappings, "Geo_Point", "geoPoint()", "GeoPointField")
        safeAddMapping(mappings, "Geo_Shape", "geoShape()", "GeoShapeField")

        // Advanced field types - may not exist in older versions
        safeAddMapping(mappings, "Auto", "auto()", "AutoField")
        safeAddMapping(mappings, "Rank_Feature", "rankFeature()", "RankFeatureField")
        safeAddMapping(mappings, "Rank_Features", "rankFeatures()", "RankFeaturesField")
        safeAddMapping(mappings, "Wildcard", "wildcard()", "WildcardField")
        safeAddMapping(mappings, "Dense_Vector", "denseVector()", "DenseVectorField")
        safeAddMapping(mappings, "Sparse_Vector", "sparseVector()", "SparseVectorField")
        safeAddMapping(mappings, "Constant_Keyword", "constantKeyword()", "ConstantKeywordField")
        safeAddMapping(mappings, "Alias", "alias()", "AliasField")
        safeAddMapping(mappings, "Version", "version()", "VersionField")
        safeAddMapping(mappings, "Murmur3", "murmur3()", "Murmur3Field")
        safeAddMapping(mappings, "Match_Only_Text", "matchOnlyText()", "MatchOnlyTextField")
        safeAddMapping(mappings, "Annotated_Text", "annotatedText()", "AnnotatedTextField")
        safeAddMapping(mappings, "Completion", "completion()", "CompletionField")
        safeAddMapping(mappings, "Join", "join()", "JoinField")

        return mappings.toMap()
    }

    /**
     * Safely adds a FieldType mapping if the enum value exists in the current version.
     */
    private fun safeAddMapping(
        mappings: MutableMap<FieldType, FieldTypeMapping>,
        fieldTypeName: String,
        delegate: String,
        className: String,
    ) {
        try {
            val fieldType = FieldType.valueOf(fieldTypeName)
            mappings[fieldType] = FieldTypeMapping(delegate, className)
        } catch (e: IllegalArgumentException) {
            // FieldType enum value doesn't exist in this version - skip it
            logger.info("Skipping unsupported FieldType: $fieldTypeName (${e.message})")
        }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        try {
            // Find all classes annotated with @Document
            val documentClasses =
                resolver
                    .getSymbolsWithAnnotation(Document::class.qualifiedName!!)
                    .filterIsInstance<KSClassDeclaration>()
                    .toList()

            if (documentClasses.isEmpty()) {
                return emptyList()
            }

            logger.info("Processing ${documentClasses.size} document classes")

            // First pass: collect all ObjectField references from all classes
            documentClasses.forEach { documentClass ->
                collectObjectFields(documentClass)
            }

            // Additional pass: collect all possible ObjectField references from entire classpath
            collectAllPossibleObjectFields(resolver)

            // Second pass: process each document class and generate QIndex classes
            documentClasses.forEach { documentClass ->
                processDocumentClass(documentClass)
            }

            // Third pass: generate all collected ObjectFields classes
            generateAllObjectFields()
        } catch (e: Exception) {
            logger.error("Error processing documents: ${e.message}")
            e.printStackTrace()
        }
        return emptyList()
    }

    private fun processDocumentClass(documentClass: KSClassDeclaration) {
        val documentAnnotation = documentClass.findAnnotation(Document::class)
        val indexName = documentAnnotation?.getArgumentValue<String>("indexName") ?: "unknown"
        val className = documentClass.simpleName.asString()
        val packageName = documentClass.packageName.asString()

        logger.info("Processing document class: $className")

        // Generate QIndex class
        val qIndexClass = generateQIndexClass(documentClass, indexName, className, packageName)

        // Write the generated QIndex file
        writeGeneratedFile(qIndexClass, packageName, "Q$className")
    }

    private fun generateQIndexClass(
        documentClass: KSClassDeclaration,
        indexName: String,
        className: String,
        packageName: String,
    ): FileSpec {
        val qIndexClassName = "${DSLConstants.Q_PREFIX}$className"
        val importContext = ImportContext()
        val typeParameterResolver = documentClass.typeParameters.toTypeParameterResolver()

        val objectBuilder = createQIndexObjectBuilder(documentClass, qIndexClassName, indexName)
        addPropertiesToObjectBuilder(documentClass, objectBuilder, importContext, typeParameterResolver)

        return createQIndexFileSpec(packageName, qIndexClassName, objectBuilder, importContext)
    }

    /**
     * Creates the TypeSpec.Builder for a QIndex class with KDoc and basic setup.
     */
    private fun createQIndexObjectBuilder(
        documentClass: KSClassDeclaration,
        qIndexClassName: String,
        indexName: String,
    ): TypeSpec.Builder =
        TypeSpec
            .objectBuilder(qIndexClassName)
            .addKdoc(
                """
                Query DSL object for Elasticsearch index '$indexName'.
                
                This class was automatically generated by QElasticsearch annotation processor
                from the source class [${documentClass.qualifiedName?.asString()}].
                
                **Do not modify this file directly.** Any changes will be overwritten
                during the next compilation. To modify the DSL structure, update the
                annotations on the source document class.
                
                @see ${documentClass.qualifiedName?.asString()}
                @generated by QElasticsearch annotation processor
                """.trimIndent(),
            ).addModifiers(KModifier.DATA)
            .superclass(ClassName(DSLConstants.DSL_PACKAGE, DSLConstants.INDEX_CLASS))
            .addSuperclassConstructorParameter("%S", indexName)
            .addOriginatingKSFile(documentClass.containingFile!!)

    /**
     * Processes all properties in the document class and adds them to the object builder.
     */
    private fun addPropertiesToObjectBuilder(
        documentClass: KSClassDeclaration,
        objectBuilder: TypeSpec.Builder,
        importContext: ImportContext,
        typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
    ) {
        val processedPropertyNames = mutableSetOf<String>()
        documentClass.getAllProperties().forEach { property ->
            val propertyName = property.simpleName.asString()
            if (propertyName !in processedPropertyNames) {
                processProperty(property, objectBuilder, importContext, typeParameterResolver)
                processedPropertyNames.add(propertyName)
            }
        }
    }

    /**
     * Creates the final FileSpec with all imports and annotations.
     */
    private fun createQIndexFileSpec(
        packageName: String,
        qIndexClassName: String,
        objectBuilder: TypeSpec.Builder,
        importContext: ImportContext,
    ): FileSpec {
        val fileBuilder =
            FileSpec
                .builder(packageName, qIndexClassName)
                .addType(objectBuilder.build())
                .addImport(DSLConstants.DSL_PACKAGE, DSLConstants.INDEX_CLASS)
                .addAnnotation(
                    AnnotationSpec
                        .builder(JvmName::class)
                        .addMember("%S", qIndexClassName)
                        .build(),
                ).indent("    ") // Use 4-space indentation for ktlint compliance

        addImportsToFileBuilder(fileBuilder, importContext)
        return fileBuilder.build()
    }

    /**
     * Adds DSL and type imports to the FileSpec builder.
     */
    private fun addImportsToFileBuilder(
        fileBuilder: FileSpec.Builder,
        importContext: ImportContext,
    ) {
        // Add DSL imports
        importContext.usedImports.forEach { className ->
            fileBuilder.addImport(DSLConstants.DSL_PACKAGE, className)
        }

        // Add type imports (full qualified names like "com.example.ParametrisedType")
        importContext.typeImports.forEach { qualifiedName ->
            if (qualifiedName.contains('.')) {
                val parts = qualifiedName.split('.')
                val className = parts.last()
                val packageName = parts.dropLast(1).joinToString(".")
                fileBuilder.addImport(packageName, className)
            }
        }
    }

    private fun processProperty(
        property: KSPropertyDeclaration,
        objectBuilder: TypeSpec.Builder,
        importContext: ImportContext,
        typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
    ) {
        val fieldAnnotation = property.findAnnotation(Field::class)
        val multiFieldAnnotation = property.findAnnotation(MultiField::class)
        val idAnnotation = property.findAnnotation(Id::class)

        val propertyName = property.simpleName.asString()

        // Skip fields with no annotations at all
        if (fieldAnnotation == null && multiFieldAnnotation == null && idAnnotation == null) {
            logger.info("Property $propertyName has no annotations, ignoring field")
            return
        }

        when {
            multiFieldAnnotation != null -> {
                // Handle multi-field
                generateMultiFieldProperty(objectBuilder, property, propertyName, multiFieldAnnotation, importContext.usedImports)
            }

            else -> {
                // Handle simple or object field
                val fieldType = determineFieldType(property, fieldAnnotation, idAnnotation)

                if (fieldType.isObjectType) {
                    // Handle object/nested field
                    generateObjectFieldProperty(objectBuilder, property, propertyName, fieldType)
                } else {
                    // Handle simple field
                    val context =
                        FieldGenerationContext(
                            objectBuilder,
                            property,
                            propertyName,
                            fieldType,
                            importContext,
                            typeParameterResolver,
                        )
                    generateSimpleFieldProperty(context)
                }
            }
        }
    }

    private fun generateSimpleFieldProperty(context: FieldGenerationContext) {
        // Handle Object/Nested fields by ensuring Q-class exists and delegating to object field handler
        when (context.fieldType.elasticsearchType) {
            FieldType.Object, FieldType.Nested -> {
                // Ensure the referenced class is registered in globalObjectFields
                ensureObjectFieldRegistered(context.fieldType)
                // Delegate to object field property generator
                return generateObjectFieldProperty(context.objectBuilder, context.property, context.propertyName, context.fieldType)
            }

            else -> {
                // Continue with normal field generation
            }
        }

        // Use existing helper functions to get field info
        val fieldClass = getFieldClass(context.fieldType.elasticsearchType)
        val basicDelegate = getFieldDelegate(context.fieldType.elasticsearchType)
        val methodName = basicDelegate.substringBefore("()")

        // Create KotlinPoet TypeName directly from KSType for better nested generic handling
        val kotlinType = context.fieldType.kotlinType.resolve()
        val typeParam = createKotlinPoetTypeName(kotlinType, context.typeParameterResolver)
        val delegateCall =
            generateGenericDelegateCallForKSType(
                methodName,
                kotlinType,
                context.typeParameterResolver,
            )

        val finalTypeName = ClassName(DSLConstants.DSL_PACKAGE, fieldClass).parameterizedBy(typeParam)

        val kdoc = generateFieldKDoc(context.property, context.fieldType)

        context.objectBuilder.addProperty(
            PropertySpec
                .builder(context.propertyName, finalTypeName)
                .addKdoc(kdoc)
                .delegate(delegateCall)
                .build(),
        )
    }

    private fun generateObjectFieldProperty(
        objectBuilder: TypeSpec.Builder,
        property: KSPropertyDeclaration,
        propertyName: String,
        fieldType: ProcessedFieldType,
    ) {
        // Find the actual class declaration to determine the correct Q-class name
        val actualClassDeclaration = findActualClassDeclaration(fieldType)
        if (actualClassDeclaration == null) {
            logger.warn("Could not find class declaration for field type: ${fieldType.kotlinTypeName}")
            return
        }

        val objectFieldKey = generateObjectFieldKey(actualClassDeclaration)
        val objectFieldInfo = globalObjectFields[objectFieldKey]
        if (objectFieldInfo == null) {
            logger.warn("No ObjectField registered for key: $objectFieldKey (type: ${fieldType.kotlinTypeName})")
            return
        }

        val objectFieldsClassName = objectFieldInfo.className
        val isNested = fieldType.elasticsearchType == FieldType.Nested

        val delegateCall =
            if (isNested) {
                "nestedField($objectFieldsClassName)"
            } else {
                "objectField($objectFieldsClassName)"
            }

        val targetPackage = objectFieldInfo.packageName
        val kdoc = generateFieldKDoc(property, fieldType, listOf("@${fieldType.elasticsearchType.name}"))

        objectBuilder.addProperty(
            PropertySpec
                .builder(propertyName, ClassName(targetPackage, objectFieldsClassName))
                .addKdoc(kdoc)
                .delegate(delegateCall)
                .build(),
        )
    }

    private fun generateMultiFieldProperty(
        objectBuilder: TypeSpec.Builder,
        property: KSPropertyDeclaration,
        propertyName: String,
        multiFieldAnnotation: KSAnnotation,
        usedImports: MutableSet<String> = mutableSetOf(),
    ) {
        val mainFieldAnnotation = multiFieldAnnotation.getArgumentValue<KSAnnotation>("mainField")

        // Handle both single @InnerField and array of @InnerField
        val innerFields =
            when (
                val otherFieldsValue =
                    multiFieldAnnotation.arguments.find { it.name?.asString() == "otherFields" }?.value
            ) {
                is List<*> -> otherFieldsValue.filterIsInstance<KSAnnotation>()
                is KSAnnotation -> listOf(otherFieldsValue)
                else -> emptyList()
            }

        val mainFieldType = extractFieldTypeFromAnnotation(mainFieldAnnotation)
        val mainFieldDelegate = getFieldDelegate(mainFieldType)

        if (innerFields.isEmpty()) {
            // Simple multi-field without inner fields - use regular field
            val simpleFieldType =
                ProcessedFieldType(
                    elasticsearchType = mainFieldType,
                    kotlinType = property.type,
                    kotlinTypeName = getSimpleTypeName(property.type),
                    isObjectType = false,
                )
            val kdoc = generateFieldKDoc(property, simpleFieldType, listOf("@MultiField"))

            objectBuilder.addProperty(
                PropertySpec
                    .builder(propertyName, ClassName("com.qelasticsearch.dsl", "Field"))
                    .addKdoc(kdoc)
                    .delegate(mainFieldDelegate)
                    .build(),
            )
        } else {
            // Multi-field with inner fields - generate MultiFieldProxy
            val mainFieldClass = getFieldClass(mainFieldType)
            usedImports.add(mainFieldClass)
            usedImports.add(DSLConstants.MULTI_FIELD_PROXY)

            val innerFieldsCode =
                "\n        " +
                    innerFields
                        .joinToString(separator = "\n        ") { innerFieldAnnotation ->
                            val suffix = innerFieldAnnotation.getArgumentValue<String>("suffix") ?: "unknown"
                            val innerFieldType = extractFieldTypeFromAnnotation(innerFieldAnnotation)
                            val innerFieldClass = getFieldClass(innerFieldType)
                            usedImports.add(innerFieldClass)
                            "field(\"$suffix\") { $innerFieldClass<String>(\"$suffix\") }"
                        } +
                    "\n    "

            val complexFieldType =
                ProcessedFieldType(
                    elasticsearchType = mainFieldType,
                    kotlinType = property.type,
                    kotlinTypeName = getSimpleTypeName(property.type),
                    isObjectType = false,
                )
            val innerFieldsList = innerFields.map { it.getArgumentValue<String>("suffix") ?: "unknown" }
            val kdoc =
                generateFieldKDoc(
                    property,
                    complexFieldType,
                    listOf("@MultiField", "inner fields: ${innerFieldsList.joinToString(", ")}"),
                )

            objectBuilder.addProperty(
                PropertySpec
                    .builder(propertyName, ClassName(DSLConstants.DSL_PACKAGE, DSLConstants.MULTI_FIELD_PROXY))
                    .addKdoc(kdoc)
                    .delegate("multiFieldProxy($mainFieldClass<String>(\"$propertyName\")) {$innerFieldsCode}")
                    .build(),
            )
        }
    }

    /**
     * Generates KDoc documentation for a generated field property.
     */
    private fun generateFieldKDoc(
        property: KSPropertyDeclaration,
        fieldType: ProcessedFieldType,
        annotations: List<String> = emptyList(),
    ): String {
        val containingClass = property.parentDeclaration as? KSClassDeclaration
        val containingClassName = containingClass?.qualifiedName?.asString() ?: "Unknown"
        val propertyName = property.simpleName.asString()
        // Use the actual property type name instead of the simplified fieldType.kotlinTypeName
        val kotlinTypeName = getFullTypeName(property.type)
        val elasticsearchType = fieldType.elasticsearchType.name

        val annotationInfo =
            if (annotations.isNotEmpty()) {
                " with ${annotations.joinToString(", ")}"
            } else {
                ""
            }

        return """
            |Elasticsearch field for property [$containingClassName.$propertyName].
            |
            |**Original Property:**
            |- Name: `$propertyName`
            |- Type: `$kotlinTypeName`
            |- Elasticsearch Type: `$elasticsearchType`$annotationInfo
            |
            |@see $containingClassName.$propertyName
            """.trimMargin()
    }

    private fun extractFieldTypeFromAnnotation(annotation: KSAnnotation?): FieldType =
        if (annotation != null) {
            extractFieldType(annotation) ?: FieldType.Text
        } else {
            FieldType.Text
        }

    /**
     * Get the DSL delegate method name for a given field type.
     * Uses the unified field type mapping to eliminate code duplication.
     */
    private fun getFieldDelegate(fieldType: FieldType): String = fieldTypeMappings[fieldType]?.delegate ?: "keyword()" // Default fallback

    /**
     * Get the DSL field class name for a given field type.
     * Uses the unified field type mapping to eliminate code duplication.
     */
    private fun getFieldClass(fieldType: FieldType): String = fieldTypeMappings[fieldType]?.className ?: "KeywordField" // Default fallback

    private fun determineFieldType(
        property: KSPropertyDeclaration,
        fieldAnnotation: KSAnnotation?,
        idAnnotation: KSAnnotation?,
    ): ProcessedFieldType {
        val propertyName = property.simpleName.asString()

        // If property has @Id annotation, treat as keyword by default
        if (idAnnotation != null) {
            logger.info("Property $propertyName has @Id annotation, using Keyword type")
            return ProcessedFieldType(
                elasticsearchType = FieldType.Keyword,
                kotlinType = property.type,
                kotlinTypeName = getSimpleTypeName(property.type),
                isObjectType = false,
            )
        }

        // If property has @Field annotation, use the specified type
        if (fieldAnnotation != null) {
            val fieldType = extractFieldType(fieldAnnotation) ?: FieldType.Auto
            logger.info("Property $propertyName has @Field annotation with type: $fieldType")

            // Handle nested/object types - including collections
            val isObjectType =
                when (fieldType) {
                    FieldType.Object, FieldType.Nested -> {
                        // For collections, check the element type
                        if (isCollectionType(getSimpleTypeName(property.type))) {
                            val elementType = getCollectionElementType(property)
                            elementType != null &&
                                !isStandardLibraryType(elementType.packageName.asString()) &&
                                (elementType.classKind == ClassKind.CLASS || elementType.classKind == ClassKind.INTERFACE) // Classes and interfaces only
                        } else {
                            // For single objects, check if it's a custom class or interface (not enum)
                            val typeDeclaration = property.type.resolve().declaration
                            if (typeDeclaration is KSClassDeclaration) {
                                val typeName = typeDeclaration.simpleName.asString()
                            }
                            typeDeclaration is KSClassDeclaration &&
                                (typeDeclaration.classKind == ClassKind.CLASS || typeDeclaration.classKind == ClassKind.INTERFACE) &&
                                !isStandardLibraryType(typeDeclaration.packageName.asString())
                        }
                    }

                    else -> false
                }

            val kotlinTypeName =
                if (isCollectionType(getSimpleTypeName(property.type))) {
                    getCollectionElementType(property)?.simpleName?.asString() ?: getSimpleTypeName(property.type)
                } else {
                    getSimpleTypeName(property.type)
                }

            return ProcessedFieldType(
                elasticsearchType = fieldType,
                kotlinType = property.type,
                kotlinTypeName = kotlinTypeName,
                isObjectType = isObjectType,
            )
        }

        // This should not happen since we check for annotations before calling this function
        error("Property $propertyName has neither @Field nor @Id annotation")
    }

    private fun isCollectionType(typeName: String): Boolean =
        typeName in
            setOf(
                "List",
                "MutableList",
                "ArrayList",
                "LinkedList",
                "Set",
                "MutableSet",
                "HashSet",
                "LinkedHashSet",
                "Collection",
                "MutableCollection",
                "Array",
                "Map",
                "MutableMap",
                "HashMap",
                "LinkedHashMap",
            )

    private fun isStandardLibraryType(packageName: String): Boolean =
        packageName.startsWith("kotlin.") ||
            packageName.startsWith("java.") ||
            packageName.startsWith("javax.") ||
            packageName == "kotlin" ||
            packageName == "java"

    @Suppress("ReturnCount")
    private fun findNestedClass(
        property: KSPropertyDeclaration,
        fieldType: ProcessedFieldType,
    ): KSClassDeclaration? {
        return try {
            val type = property.type.resolve()
            val declaration = type.declaration

            logger.info("Looking for nested class: ${fieldType.kotlinTypeName}, declaration: ${declaration.qualifiedName?.asString()}")

            if (declaration is KSClassDeclaration) {
                return declaration
            }

            // For generic types like List<T>, try to get the type argument
            val typeArguments = type.arguments
            if (typeArguments.isNotEmpty()) {
                val firstArg = typeArguments.first()
                val argType = firstArg.type?.resolve()
                if (argType?.declaration is KSClassDeclaration) {
                    logger.info("Found generic type argument: ${argType.declaration.qualifiedName?.asString()}")
                    return argType.declaration as KSClassDeclaration
                }
            }

            null
        } catch (e: Exception) {
            logger.error("Error finding nested class for ${fieldType.kotlinTypeName}: ${e.message}")
            null
        }
    }

    private fun getSimpleTypeName(type: KSTypeReference): String =
        type
            .resolve()
            .declaration.simpleName
            .asString()

    private fun getCollectionElementType(property: KSPropertyDeclaration): KSClassDeclaration? =
        try {
            val type = property.type.resolve()
            val typeArguments = type.arguments
            if (typeArguments.isNotEmpty()) {
                val firstArg = typeArguments.first()
                val argType = firstArg.type?.resolve()
                argType?.declaration as? KSClassDeclaration
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error getting collection element type for ${property.simpleName.asString()}: ${e.message}")
            null
        }

    private val generatedFiles = mutableSetOf<String>()
    private val globalObjectFields = mutableMapOf<String, ObjectFieldInfo>()

    private fun writeGeneratedFile(
        fileSpec: FileSpec,
        packageName: String,
        className: String,
    ) {
        try {
            val fileKey = "$packageName.$className"
            if (fileKey in generatedFiles) {
                logger.info("Skipping duplicate file generation for: $fileKey")
                return
            }

            val outputFile =
                codeGenerator.createNewFile(
                    dependencies = Dependencies(false), // No specific dependencies for now
                    packageName = packageName,
                    fileName = className,
                )

            outputFile.bufferedWriter().use { writer ->
                fileSpec.writeTo(writer)
            }

            generatedFiles.add(fileKey)
            logger.info("Generated file: $packageName.$className")
        } catch (e: Exception) {
            logger.error("Error writing file $className: ${e.message}")
        }
    }

    private data class ProcessedFieldType(
        val elasticsearchType: FieldType,
        val kotlinType: KSTypeReference,
        val kotlinTypeName: String,
        val isObjectType: Boolean,
    )

    /**
     * Context for field property generation.
     */
    private data class FieldGenerationContext(
        val objectBuilder: TypeSpec.Builder,
        val property: KSPropertyDeclaration,
        val propertyName: String,
        val fieldType: ProcessedFieldType,
        val importContext: ImportContext,
        val typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
    )

    private data class ObjectFieldInfo(
        val className: String,
        val packageName: String,
        val classDeclaration: KSClassDeclaration,
        val qualifiedName: String, // Add qualified name for proper identification
    )

    /**
     * Generate a unique Q-class name for a given class declaration.
     * For nested classes, includes parent class name to avoid conflicts.
     */
    private fun generateUniqueQClassName(classDeclaration: KSClassDeclaration): String {
        val simpleName = classDeclaration.simpleName.asString()
        val parentClass = classDeclaration.parentDeclaration as? KSClassDeclaration

        return if (parentClass != null) {
            // For nested classes: Q + ParentClassName + NestedClassName
            "Q${parentClass.simpleName.asString()}$simpleName"
        } else {
            // For top-level classes: Q + ClassName
            "Q$simpleName"
        }
    }

    /**
     * Generate a unique key for the globalObjectFields map using qualified names.
     * This prevents collisions between nested and top-level classes with same simple name.
     */
    private fun generateObjectFieldKey(classDeclaration: KSClassDeclaration): String =
        classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString()

    /**
     * Find the actual class declaration for a field type.
     * This handles both collection types and direct object types.
     */
    private fun findActualClassDeclaration(fieldType: ProcessedFieldType): KSClassDeclaration? {
        val kotlinType = fieldType.kotlinType.resolve()

        return if (isCollectionType(getSimpleTypeName(fieldType.kotlinType))) {
            // For collections, get the element type
            val typeArguments = kotlinType.arguments
            if (typeArguments.isNotEmpty()) {
                val firstArg = typeArguments.first()
                firstArg.type?.resolve()?.declaration as? KSClassDeclaration
            } else {
                null
            }
        } else {
            // For direct object types
            kotlinType.declaration as? KSClassDeclaration
        }
    }

    /**
     * Generate a generic delegate call directly from KSType using KotlinPoet KSP interop.
     */
    private fun generateGenericDelegateCallForKSType(
        methodName: String,
        kotlinType: KSType,
        typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
    ): String {
        val typeName = kotlinType.toTypeName(typeParameterResolver)

        // Create simplified type string without adding imports since KotlinPoet handles them from property type
        val simplifiedTypeString = simplifyTypeNameWithoutImports(typeName)
        return "$methodName<$simplifiedTypeString>()"
    }

    /**
     * Simplify TypeName to string without adding imports (for delegate calls where KotlinPoet handles imports).
     */
    private fun simplifyTypeNameWithoutImports(typeName: com.squareup.kotlinpoet.TypeName): String =
        when (typeName) {
            is ClassName -> typeName.simpleName
            is com.squareup.kotlinpoet.ParameterizedTypeName -> {
                val baseSimpleName = simplifyTypeNameWithoutImports(typeName.rawType)
                val typeArgs =
                    typeName.typeArguments.joinToString(", ") { arg ->
                        simplifyTypeNameWithoutImports(arg)
                    }
                "$baseSimpleName<$typeArgs>"
            }
            else -> typeName.toString()
        }

    /**
     * Extract necessary imports from TypeName and return simplified type string.
     */
    private fun extractImportsAndSimplifyTypeName(
        typeName: com.squareup.kotlinpoet.TypeName,
        usedImports: MutableSet<String>,
    ): String =
        when (typeName) {
            is ClassName -> {
                // Add import for this class and return simple name
                // Skip kotlin.* and java.lang.* packages as they don't need imports
                if (typeName.packageName.isNotEmpty() &&
                    !typeName.packageName.startsWith("kotlin") &&
                    !typeName.packageName.startsWith("java.lang")
                ) {
                    usedImports.add("${typeName.packageName}.${typeName.simpleName}")
                }
                typeName.simpleName
            }

            is com.squareup.kotlinpoet.ParameterizedTypeName -> {
                // Handle parameterized types recursively
                val baseSimpleName = extractImportsAndSimplifyTypeName(typeName.rawType, usedImports)
                val typeArgs =
                    typeName.typeArguments.joinToString(", ") { arg ->
                        extractImportsAndSimplifyTypeName(arg, usedImports)
                    }
                "$baseSimpleName<$typeArgs>"
            }

            else -> {
                // For other types, use the string representation
                typeName.toString()
            }
        }

    /**
     * Create a KotlinPoet TypeName directly from KSType using KotlinPoet KSP interop utilities.
     */
    private fun createKotlinPoetTypeName(
        kotlinType: KSType,
        typeParameterResolver: com.squareup.kotlinpoet.ksp.TypeParameterResolver,
    ): com.squareup.kotlinpoet.TypeName {
        // Use KotlinPoet KSP interop utility for seamless type conversion with TypeParameterResolver
        return kotlinType.toTypeName(typeParameterResolver)
    }

    private fun collectObjectFields(documentClass: KSClassDeclaration) {
        documentClass.getAllProperties().forEach { property ->
            val fieldAnnotation = property.findAnnotation(Field::class)
            val idAnnotation = property.findAnnotation(Id::class)
            // Skip fields with no relevant annotations
            if (fieldAnnotation == null && idAnnotation == null) {
                return@forEach
            }
            val fieldType = determineFieldType(property, fieldAnnotation, idAnnotation)

            if (fieldType.isObjectType) {
                // For collections, get the element type directly
                val nestedClass =
                    if (isCollectionType(getSimpleTypeName(property.type))) {
                        getCollectionElementType(property)
                    } else {
                        property.type.resolve().declaration as? KSClassDeclaration
                    }

                if (nestedClass != null) {
                    val targetPackage = nestedClass.packageName.asString()

                    // Skip standard library types
                    if (isStandardLibraryType(targetPackage)) {
                        logger.info("Skipping standard library type: ${fieldType.kotlinTypeName} in package $targetPackage")
                        return@forEach
                    }

                    val className = generateUniqueQClassName(nestedClass)
                    val objectFieldKey = generateObjectFieldKey(nestedClass)

                    globalObjectFields[objectFieldKey] =
                        ObjectFieldInfo(
                            className = className,
                            packageName = targetPackage,
                            classDeclaration = nestedClass,
                            qualifiedName = nestedClass.qualifiedName?.asString() ?: nestedClass.simpleName.asString(),
                        )

                    logger.info("Registered ObjectField: ${fieldType.kotlinTypeName} -> $targetPackage.$className")

                    // Recursively collect from nested class
                    collectObjectFieldsFromClass(nestedClass)
                }
            }
        }
    }

    private fun collectAllPossibleObjectFields(resolver: Resolver) {
        try {
            // Rule: Generate Q-class for any class that has at least one Elasticsearch annotation
            // This includes both top-level classes and nested static classes
            val allClasses = mutableListOf<KSClassDeclaration>()

            resolver.getAllFiles().flatMap { it.declarations }.forEach { declaration ->
                if (declaration is KSClassDeclaration && declaration.classKind == ClassKind.CLASS) {
                    allClasses.add(declaration)
                    // Also add nested classes
                    collectNestedClasses(declaration, allClasses)
                }
            }

            val filteredClasses = allClasses.filter { !isStandardLibraryType(it.packageName.asString()) }

            logger.info("Scanning ${filteredClasses.size} classes (including nested) for Elasticsearch annotations...")

            filteredClasses.forEach { classDeclaration ->
                val className = classDeclaration.simpleName.asString()
                val packageName = classDeclaration.packageName.asString()

                val hasElasticsearchAnnotations =
                    classDeclaration.getAllProperties().any { property ->
                        val hasField = property.findAnnotation(Field::class) != null
                        val hasId = property.findAnnotation(Id::class) != null
                        val hasMultiField = property.findAnnotation(MultiField::class) != null
                        hasField || hasId || hasMultiField
                    }

                val objectFieldKey = generateObjectFieldKey(classDeclaration)
                val shouldRegister =
                    when {
                        objectFieldKey in globalObjectFields -> {
                            logger.info("Class $className already registered, skipping")
                            false
                        }

                        hasElasticsearchAnnotations -> {
                            logger.info("Class $className has Elasticsearch annotations, will register")
                            true
                        }

                        else -> {
                            // Check specifically for the missing classes
                            false
                        }
                    }

                if (shouldRegister) {
                    val uniqueClassName = generateUniqueQClassName(classDeclaration)
                    globalObjectFields[objectFieldKey] =
                        ObjectFieldInfo(
                            className = uniqueClassName,
                            packageName = packageName,
                            classDeclaration = classDeclaration,
                            qualifiedName =
                                classDeclaration.qualifiedName?.asString()
                                    ?: classDeclaration.simpleName.asString(),
                        )
                    logger.info("Registered ObjectField: $className -> $packageName.Q$className")
                }
            }

            logger.info("Total registered ObjectFields: ${globalObjectFields.size}")
            globalObjectFields.keys.forEach { className ->
                logger.info("  - $className")
            }
        } catch (e: Exception) {
            logger.warn("Error collecting all possible object fields: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun collectNestedClasses(
        parentClass: KSClassDeclaration,
        allClasses: MutableList<KSClassDeclaration>,
    ) {
        parentClass.declarations.filterIsInstance<KSClassDeclaration>().forEach { nestedClass ->
            if (nestedClass.classKind == ClassKind.CLASS) {
                allClasses.add(nestedClass)
                logger.info("Found nested class: ${nestedClass.simpleName.asString()} in ${parentClass.simpleName.asString()}")
                // Recursively collect nested classes
                collectNestedClasses(nestedClass, allClasses)
            }
        }
    }

    private fun collectObjectFieldsFromClass(classDeclaration: KSClassDeclaration) {
        classDeclaration.getAllProperties().forEach { property ->
            val fieldAnnotation = property.findAnnotation(Field::class)
            val idAnnotation = property.findAnnotation(Id::class)

            // Skip fields with no relevant annotations
            if (fieldAnnotation == null && idAnnotation == null) {
                return@forEach
            }
            val fieldType = determineFieldType(property, fieldAnnotation, idAnnotation)

            if (fieldType.isObjectType) {
                val nestedClass = findNestedClass(property, fieldType)
                if (nestedClass != null) {
                    val targetPackage = nestedClass.packageName.asString()

                    // Skip standard library types
                    if (isStandardLibraryType(targetPackage)) {
                        logger.info("Skipping standard library type: ${fieldType.kotlinTypeName} in package $targetPackage")
                        return@forEach
                    }

                    val className = generateUniqueQClassName(nestedClass)
                    val objectFieldKey = generateObjectFieldKey(nestedClass)

                    if (objectFieldKey !in globalObjectFields) {
                        globalObjectFields[objectFieldKey] =
                            ObjectFieldInfo(
                                className = className,
                                packageName = targetPackage,
                                classDeclaration = nestedClass,
                                qualifiedName =
                                    nestedClass.qualifiedName?.asString()
                                        ?: nestedClass.simpleName.asString(),
                            )

                        logger.info("Registered nested ObjectField: ${fieldType.kotlinTypeName} -> $targetPackage.$className")

                        // Recursively collect from this nested class too
                        collectObjectFieldsFromClass(nestedClass)
                    }
                }
            }
        }
    }

    private fun generateAllObjectFields() {
        // Create a copy of the values to avoid ConcurrentModificationException
        val objectFieldsToGenerate = globalObjectFields.values.toList()
        objectFieldsToGenerate.forEach { objectFieldInfo ->
            val fileSpec = generateObjectFieldsClassFromInfo(objectFieldInfo)
            writeGeneratedFile(fileSpec, objectFieldInfo.packageName, objectFieldInfo.className)
        }
    }

    private fun generateObjectFieldsClassFromInfo(objectFieldInfo: ObjectFieldInfo): FileSpec {
        val importContext = ImportContext()

        // Create TypeParameterResolver for ObjectFields class
        val typeParameterResolver = objectFieldInfo.classDeclaration.typeParameters.toTypeParameterResolver()

        val objectBuilder =
            TypeSpec
                .objectBuilder(objectFieldInfo.className)
                .addKdoc(
                    """
                    Query DSL object fields for class [${objectFieldInfo.classDeclaration.qualifiedName?.asString()}].
                    
                    This class was automatically generated by QElasticsearch annotation processor
                    from the source class [${objectFieldInfo.classDeclaration.qualifiedName?.asString()}].
                    
                    **Do not modify this file directly.** Any changes will be overwritten
                    during the next compilation. To modify the DSL structure, update the
                    annotations on the source class.
                    
                    @see ${objectFieldInfo.classDeclaration.qualifiedName?.asString()}
                    @generated by QElasticsearch annotation processor
                    """.trimIndent(),
                ).superclass(ClassName(DSLConstants.DSL_PACKAGE, DSLConstants.OBJECT_FIELDS_CLASS))
                .addOriginatingKSFile(objectFieldInfo.classDeclaration.containingFile!!)

        val processedPropertyNames = mutableSetOf<String>()
        objectFieldInfo.classDeclaration.getAllProperties().forEach { property ->
            val propertyName = property.simpleName.asString()
            if (propertyName !in processedPropertyNames) {
                processProperty(property, objectBuilder, importContext, typeParameterResolver)
                processedPropertyNames.add(propertyName)
            }
        }

        val fileBuilder =
            FileSpec
                .builder(objectFieldInfo.packageName, objectFieldInfo.className)
                .addType(objectBuilder.build())
                .addImport(DSLConstants.DSL_PACKAGE, DSLConstants.OBJECT_FIELDS_CLASS)
                .indent("    ") // Use 4-space indentation for ktlint compliance

        // Add only the imports that are actually used
        importContext.usedImports.forEach { className ->
            fileBuilder.addImport(DSLConstants.DSL_PACKAGE, className)
        }

        // Add type imports (full qualified names like "com.example.ParametrisedType")
        importContext.typeImports.forEach { qualifiedName ->
            if (qualifiedName.contains('.')) {
                val parts = qualifiedName.split('.')
                val className = parts.last()
                val packageName = parts.dropLast(1).joinToString(".")
                fileBuilder.addImport(packageName, className)
            }
        }

        return fileBuilder.build()
    }

    /**
     * Ensures that the referenced class from an Object/Nested field is registered in globalObjectFields.
     * This method will create a Q-class for any referenced class, even if it has no @Field annotations.
     */
    @Suppress("ReturnCount")
    private fun ensureObjectFieldRegistered(fieldType: ProcessedFieldType) {
        val actualClassDeclaration = findActualClassDeclaration(fieldType)
        if (actualClassDeclaration == null) {
            logger.warn("Could not find class declaration for field type: ${fieldType.kotlinTypeName}")
            return
        }

        val objectFieldKey = generateObjectFieldKey(actualClassDeclaration)

        // Check if already registered
        if (objectFieldKey in globalObjectFields) {
            logger.info("Class ${actualClassDeclaration.simpleName.asString()} already registered as Q-class")
            return
        }

        val targetPackage = actualClassDeclaration.packageName.asString()

        // Skip standard library types
        if (isStandardLibraryType(targetPackage)) {
            logger.info("Skipping standard library type: ${fieldType.kotlinTypeName} in package $targetPackage")
            return
        }

        // Skip enums - they should not have Q-classes generated
        // Allow classes and interfaces, but skip enums and other types
        if (actualClassDeclaration.classKind != ClassKind.CLASS && actualClassDeclaration.classKind != ClassKind.INTERFACE) {
            logger.info("Skipping non-class/interface type: ${fieldType.kotlinTypeName} (kind: ${actualClassDeclaration.classKind})")
            return
        }

        val className = generateUniqueQClassName(actualClassDeclaration)

        globalObjectFields[objectFieldKey] =
            ObjectFieldInfo(
                className = className,
                packageName = targetPackage,
                classDeclaration = actualClassDeclaration,
                qualifiedName =
                    actualClassDeclaration.qualifiedName?.asString()
                        ?: actualClassDeclaration.simpleName.asString(),
            )

        logger
            .info("Registered new Q-class for Object/Nested field: ${fieldType.kotlinTypeName} -> $targetPackage.$className")

        // Recursively collect from this class to handle nested object fields
        collectObjectFieldsFromClass(actualClassDeclaration)
    }

    // Extension functions for KSP annotation handling
    private fun KSClassDeclaration.findAnnotation(annotationClass: kotlin.reflect.KClass<*>): KSAnnotation? =
        annotations.find {
            it.annotationType
                .resolve()
                .declaration.qualifiedName
                ?.asString() == annotationClass.qualifiedName
        }

    /**
     * Gets the full type name including generics from a KSTypeReference.
     * Examples: List<String> -> "List<String>", ParametrizedType<String> -> "ParametrizedType<String>"
     */
    private fun getFullTypeName(typeReference: KSTypeReference): String {
        val resolvedType = typeReference.resolve()
        return buildTypeString(resolvedType)
    }

    /**
     * Recursively builds a type string with generics.
     */
    private fun buildTypeString(type: KSType): String {
        val declaration = type.declaration
        val simpleName = declaration.simpleName.asString()

        val arguments = type.arguments
        return if (arguments.isNotEmpty()) {
            val argStrings =
                arguments.mapNotNull { arg ->
                    when (arg.variance) {
                        Variance.INVARIANT -> {
                            arg.type?.let { buildTypeString(it.resolve()) }
                        }
                        else -> "*" // For wildcards and other variance types
                    }
                }
            "$simpleName<${argStrings.joinToString(", ")}>"
        } else {
            simpleName
        }
    }

    private fun KSPropertyDeclaration.findAnnotation(annotationClass: kotlin.reflect.KClass<*>): KSAnnotation? =
        annotations.find {
            it.annotationType
                .resolve()
                .declaration.qualifiedName
                ?.asString() == annotationClass.qualifiedName
        }

    private fun extractFieldType(fieldAnnotation: KSAnnotation): FieldType? =
        try {
            val typeArgument = fieldAnnotation.arguments.find { it.name?.asString() == "type" }
            val value = typeArgument?.value

            logger.info("Extracting FieldType from argument: $value (type: ${value?.javaClass?.simpleName})")

            when (value) {
                is KSType -> {
                    // Handle enum reference - this is usually how enums are represented in KSP
                    val enumName = value.declaration.simpleName.asString()
                    logger.info("Found KSType enum value: $enumName")
                    FieldType.entries.find { it.name == enumName }
                }

                is String -> {
                    // Handle string representation
                    logger.info("Found String enum value: $value")
                    FieldType.entries.find { it.name == value }
                }
                // Handle KSP's representation of enum constants
                else -> {
                    // Try to get the enum value by looking at the qualified name
                    val valueStr = value.toString()
                    logger.info("Trying to parse enum from string representation: $valueStr")

                    // Extract enum name from patterns like "FieldType.Text" or just "Text"
                    val enumName =
                        when {
                            valueStr.contains("FieldType.") -> valueStr.substringAfter("FieldType.")
                            valueStr.contains(".") -> valueStr.substringAfterLast(".")
                            else -> valueStr
                        }

                    logger.info("Extracted enum name: $enumName")
                    FieldType.entries.find { it.name == enumName }
                }
            }
        } catch (e: Exception) {
            logger.error("Error extracting FieldType: ${e.message}")
            e.printStackTrace()
            null
        }

    private inline fun <reified T> KSAnnotation.getArgumentValue(name: String): T? = arguments.find { it.name?.asString() == name }?.value as? T
}
