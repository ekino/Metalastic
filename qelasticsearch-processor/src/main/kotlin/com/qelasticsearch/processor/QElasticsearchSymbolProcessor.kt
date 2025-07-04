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
import com.qelasticsearch.dsl.BinaryField
import com.qelasticsearch.dsl.BooleanField
import com.qelasticsearch.dsl.ByteField
import com.qelasticsearch.dsl.ConstantKeywordField
import com.qelasticsearch.dsl.DateField
import com.qelasticsearch.dsl.DateNanosField
import com.qelasticsearch.dsl.DateRangeField
import com.qelasticsearch.dsl.DoubleField
import com.qelasticsearch.dsl.DoubleRangeField
import com.qelasticsearch.dsl.FlattenedField
import com.qelasticsearch.dsl.FloatField
import com.qelasticsearch.dsl.FloatRangeField
import com.qelasticsearch.dsl.HalfFloatField
import com.qelasticsearch.dsl.IntegerField
import com.qelasticsearch.dsl.IntegerRangeField
import com.qelasticsearch.dsl.IpField
import com.qelasticsearch.dsl.IpRangeField
import com.qelasticsearch.dsl.KeywordField
import com.qelasticsearch.dsl.LongField
import com.qelasticsearch.dsl.LongRangeField
import com.qelasticsearch.dsl.PercolatorField
import com.qelasticsearch.dsl.RankFeatureField
import com.qelasticsearch.dsl.ScaledFloatField
import com.qelasticsearch.dsl.ShortField
import com.qelasticsearch.dsl.TextField
import com.qelasticsearch.dsl.TokenCountField
import com.qelasticsearch.dsl.WildcardField
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.MultiField

/**
 * KSP-based annotation processor that generates type-safe Elasticsearch DSL classes
 * from Spring Data Elasticsearch @Document annotated classes.
 */
class QElasticsearchSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
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

            return emptyList()
        } catch (e: Exception) {
            logger.error("Error processing documents: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
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
        val qIndexClassName = "Q$className"
        val usedImports = mutableSetOf<String>()

        // Create the object declaration with JavaDoc
        val objectBuilder =
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
                ).addModifiers() // Explicit empty modifiers to prevent default public
                .superclass(ClassName("com.qelasticsearch.dsl", "Index"))
                .addSuperclassConstructorParameter("%S", indexName)

        // Process all properties in the document class, tracking names to avoid duplicates
        val processedPropertyNames = mutableSetOf<String>()
        documentClass.getAllProperties().forEach { property ->
            val propertyName = property.simpleName.asString()
            if (propertyName !in processedPropertyNames) {
                processProperty(property, objectBuilder, packageName, usedImports)
                processedPropertyNames.add(propertyName)
            }
        }

        val fileBuilder =
            FileSpec
                .builder(packageName, qIndexClassName)
                .addType(objectBuilder.build())
                .addImport("com.qelasticsearch.dsl", "Index")
                .addAnnotation(
                    AnnotationSpec
                        .builder(JvmName::class)
                        .addMember("%S", qIndexClassName)
                        .build(),
                ).indent("    ") // Use 4-space indentation for ktlint compliance

        // Add only the imports that are actually used
        usedImports.forEach { className ->
            fileBuilder.addImport("com.qelasticsearch.dsl", className)
        }

        return fileBuilder.build()
    }

    private fun processProperty(
        property: KSPropertyDeclaration,
        objectBuilder: TypeSpec.Builder,
        packageName: String,
        usedImports: MutableSet<String> = mutableSetOf(),
    ) {
        val fieldAnnotation = property.findAnnotation(Field::class)
        val multiFieldAnnotation = property.findAnnotation(MultiField::class)
        val idAnnotation = property.findAnnotation(Id::class)

        val propertyName = property.simpleName.asString()
        val fieldType = determineFieldType(property, fieldAnnotation, idAnnotation)

        when {
            multiFieldAnnotation != null -> {
                // Handle multi-field
                generateMultiFieldProperty(objectBuilder, propertyName, multiFieldAnnotation, usedImports)
            }

            fieldType.isObjectType -> {
                // Handle object/nested field
                generateObjectFieldProperty(objectBuilder, propertyName, fieldType, packageName)
            }

            else -> {
                // Handle simple field
                generateSimpleFieldProperty(objectBuilder, propertyName, fieldType, usedImports)
            }
        }
    }

    private fun generateSimpleFieldProperty(
        objectBuilder: TypeSpec.Builder,
        propertyName: String,
        fieldType: ProcessedFieldType,
        usedImports: MutableSet<String> = mutableSetOf(),
    ) {
        val (fieldClass, delegateCall) =
            when (fieldType.elasticsearchType) {
                FieldType.Text -> TextField::class to "text()"
                FieldType.Keyword -> KeywordField::class to "keyword()"
                FieldType.Long -> LongField::class to "long()"
                FieldType.Integer -> IntegerField::class to "integer()"
                FieldType.Short -> ShortField::class to "short()"
                FieldType.Byte -> ByteField::class to "byte()"
                FieldType.Double -> DoubleField::class to "double()"
                FieldType.Float -> FloatField::class to "float()"
                FieldType.Half_Float -> HalfFloatField::class to "halfFloat()"
                FieldType.Scaled_Float -> ScaledFloatField::class to "scaledFloat()"
                FieldType.Date -> DateField::class to "date()"
                FieldType.Date_Nanos -> DateNanosField::class to "dateNanos()"
                FieldType.Boolean -> BooleanField::class to "boolean()"
                FieldType.Binary -> BinaryField::class to "binary()"
                FieldType.Ip -> IpField::class to "ip()"
                FieldType.TokenCount -> TokenCountField::class to "tokenCount()"
                FieldType.Percolator -> PercolatorField::class to "percolator()"
                FieldType.Flattened -> FlattenedField::class to "flattened()"
                FieldType.Rank_Feature -> RankFeatureField::class to "rankFeature()"
                FieldType.Rank_Features -> RankFeatureField::class to "rankFeatures()"
                FieldType.Wildcard -> WildcardField::class to "wildcard()"
                FieldType.Constant_Keyword -> ConstantKeywordField::class to "constantKeyword()"
                FieldType.Integer_Range -> IntegerRangeField::class to "integerRange()"
                FieldType.Float_Range -> FloatRangeField::class to "floatRange()"
                FieldType.Long_Range -> LongRangeField::class to "longRange()"
                FieldType.Double_Range -> DoubleRangeField::class to "doubleRange()"
                FieldType.Date_Range -> DateRangeField::class to "dateRange()"
                FieldType.Ip_Range -> IpRangeField::class to "ipRange()"
                FieldType.Object -> {
                    logger.info(
                        "Object field '$propertyName' of type '${fieldType.kotlinTypeName}' has no QObjectField, using UnknownObjectFields",
                    )
                    return generateUnknownObjectFieldProperty(
                        objectBuilder,
                        propertyName,
                        false,
                        usedImports,
                    )
                }

                FieldType.Nested -> {
                    logger.info(
                        "Nested field '$propertyName' of type '${fieldType.kotlinTypeName}' has no QObjectField, using UnknownNestedFields",
                    )
                    return generateUnknownObjectFieldProperty(objectBuilder, propertyName, true, usedImports)
                }

                else -> KeywordField::class to "keyword()" // Default fallback
            }

        objectBuilder.addProperty(
            PropertySpec
                .builder(
                    propertyName,
                    ClassName(
                        fieldClass.qualifiedName.orEmpty().removeSuffix(".${fieldClass.simpleName}"),
                        fieldClass.simpleName!!,
                    ),
                ).addModifiers() // Explicit empty modifiers to prevent default public
                .delegate(delegateCall)
                .build(),
        )
    }

    private fun generateObjectFieldProperty(
        objectBuilder: TypeSpec.Builder,
        propertyName: String,
        fieldType: ProcessedFieldType,
        packageName: String,
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

        objectBuilder.addProperty(
            PropertySpec
                .builder(propertyName, ClassName(targetPackage, objectFieldsClassName))
                .addModifiers() // Explicit empty modifiers to prevent default public
                .delegate(delegateCall)
                .build(),
        )
    }

    private fun generateUnknownObjectFieldProperty(
        objectBuilder: TypeSpec.Builder,
        propertyName: String,
        isNested: Boolean,
        usedImports: MutableSet<String>,
    ) {
        val className = if (isNested) "UnknownNestedFields" else "UnknownObjectFields"
        val delegateCall =
            if (isNested) {
                "nestedField($className)"
            } else {
                "objectField($className)"
            }

        usedImports.add(className)

        objectBuilder.addProperty(
            PropertySpec
                .builder(propertyName, ClassName("com.qelasticsearch.dsl", className))
                .addModifiers() // Explicit empty modifiers to prevent default public
                .delegate(delegateCall)
                .build(),
        )
    }

    private fun generateMultiFieldProperty(
        objectBuilder: TypeSpec.Builder,
        propertyName: String,
        multiFieldAnnotation: KSAnnotation,
        usedImports: MutableSet<String> = mutableSetOf(),
    ) {
        val mainFieldAnnotation = multiFieldAnnotation.getArgumentValue<KSAnnotation>("mainField")
        val innerFields = multiFieldAnnotation.getArgumentValue<List<KSAnnotation>>("otherFields") ?: emptyList()

        val mainFieldType = extractFieldTypeFromAnnotation(mainFieldAnnotation)
        val mainFieldDelegate = getFieldDelegate(mainFieldType)

        if (innerFields.isEmpty()) {
            // Simple multi-field without inner fields - use regular field
            objectBuilder.addProperty(
                PropertySpec
                    .builder(propertyName, ClassName("com.qelasticsearch.dsl", "Field"))
                    .addModifiers() // Explicit empty modifiers to prevent default public
                    .delegate(mainFieldDelegate)
                    .build(),
            )
        } else {
            // Multi-field with inner fields - generate MultiFieldProxy
            val mainFieldClass = getFieldClass(mainFieldType)
            usedImports.add(mainFieldClass)
            usedImports.add("MultiFieldProxy")

            val innerFieldsCode =
                " " +
                    innerFields
                        .joinToString(separator = "; ") { innerFieldAnnotation ->
                            val suffix = innerFieldAnnotation.getArgumentValue<String>("suffix") ?: "unknown"
                            val innerFieldType = extractFieldTypeFromAnnotation(innerFieldAnnotation)
                            val innerFieldClass = getFieldClass(innerFieldType)
                            usedImports.add(innerFieldClass)
                            "field(\"$suffix\") { $innerFieldClass(\"$suffix\") }"
                        } +
                    " "

            objectBuilder.addProperty(
                PropertySpec
                    .builder(propertyName, ClassName("com.qelasticsearch.dsl", "MultiFieldProxy"))
                    .addModifiers() // Explicit empty modifiers to prevent default public
                    .delegate("multiFieldProxy($mainFieldClass(\"$propertyName\")) {$innerFieldsCode}")
                    .build(),
            )
        }
    }

    private fun extractFieldTypeFromAnnotation(annotation: KSAnnotation?): FieldType =
        if (annotation != null) {
            extractFieldType(annotation) ?: FieldType.Text
        } else {
            FieldType.Text
        }

    private fun getFieldDelegate(fieldType: FieldType): String =
        when (fieldType) {
            FieldType.Text -> "text()"
            FieldType.Keyword -> "keyword()"
            FieldType.Long -> "long()"
            FieldType.Integer -> "integer()"
            FieldType.Short -> "short()"
            FieldType.Byte -> "byte()"
            FieldType.Double -> "double()"
            FieldType.Float -> "float()"
            FieldType.Half_Float -> "halfFloat()"
            FieldType.Scaled_Float -> "scaledFloat()"
            FieldType.Date -> "date()"
            FieldType.Date_Nanos -> "dateNanos()"
            FieldType.Boolean -> "boolean()"
            FieldType.Binary -> "binary()"
            FieldType.Ip -> "ip()"
            FieldType.TokenCount -> "tokenCount()"
            FieldType.Percolator -> "percolator()"
            FieldType.Flattened -> "flattened()"
            FieldType.Rank_Feature -> "rankFeature()"
            FieldType.Rank_Features -> "rankFeatures()"
            FieldType.Wildcard -> "wildcard()"
            FieldType.Constant_Keyword -> "constantKeyword()"
            FieldType.Integer_Range -> "integerRange()"
            FieldType.Float_Range -> "floatRange()"
            FieldType.Long_Range -> "longRange()"
            FieldType.Double_Range -> "doubleRange()"
            FieldType.Date_Range -> "dateRange()"
            FieldType.Ip_Range -> "ipRange()"
            else -> "keyword()" // Default fallback
        }

    private fun getFieldClass(fieldType: FieldType): String =
        when (fieldType) {
            FieldType.Text -> "TextField"
            FieldType.Keyword -> "KeywordField"
            FieldType.Long -> "LongField"
            FieldType.Integer -> "IntegerField"
            FieldType.Short -> "ShortField"
            FieldType.Byte -> "ByteField"
            FieldType.Double -> "DoubleField"
            FieldType.Float -> "FloatField"
            FieldType.Half_Float -> "HalfFloatField"
            FieldType.Scaled_Float -> "ScaledFloatField"
            FieldType.Date -> "DateField"
            FieldType.Date_Nanos -> "DateNanosField"
            FieldType.Boolean -> "BooleanField"
            FieldType.Binary -> "BinaryField"
            FieldType.Ip -> "IpField"
            FieldType.TokenCount -> "TokenCountField"
            FieldType.Percolator -> "PercolatorField"
            FieldType.Flattened -> "FlattenedField"
            FieldType.Rank_Feature -> "RankFeatureField"
            FieldType.Rank_Features -> "RankFeaturesField"
            FieldType.Wildcard -> "WildcardField"
            FieldType.Constant_Keyword -> "ConstantKeywordField"
            FieldType.Integer_Range -> "IntegerRangeField"
            FieldType.Float_Range -> "FloatRangeField"
            FieldType.Long_Range -> "LongRangeField"
            FieldType.Double_Range -> "DoubleRangeField"
            FieldType.Date_Range -> "DateRangeField"
            FieldType.Ip_Range -> "IpRangeField"
            else -> "KeywordField" // Default fallback
        }

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
                                elementType.classKind == ClassKind.CLASS // Not enum, interface, etc.
                        } else {
                            // For single objects, check if it's a custom class (not enum)
                            val typeDeclaration = property.type.resolve().declaration
                            if (typeDeclaration is KSClassDeclaration) {
                                val typeName = typeDeclaration.simpleName.asString()
                                logger.info(
                                    "Checking type $typeName: classKind=${typeDeclaration.classKind}, package=${typeDeclaration.packageName.asString()}",
                                )
                                if (typeName == "EventTarget") {
                                    logger.warn(
                                        "Found EventTarget: classKind=${typeDeclaration.classKind}, isEnum=${typeDeclaration.classKind == ClassKind.ENUM_CLASS}",
                                    )
                                }
                            }
                            typeDeclaration is KSClassDeclaration &&
                                typeDeclaration.classKind == ClassKind.CLASS &&
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

        // Auto-detect field type based on Kotlin type
        logger.info("Property $propertyName has no annotations, auto-detecting type")
        return autoDetectFieldType(property)
    }

    private fun autoDetectFieldType(property: KSPropertyDeclaration): ProcessedFieldType {
        val kotlinType = property.type
        val typeName =
            kotlinType
                .resolve()
                .declaration.simpleName
                .asString()

        val elasticsearchType =
            when (typeName) {
                "String" -> FieldType.Text
                "Long" -> FieldType.Long
                "Int", "Integer" -> FieldType.Integer
                "Short" -> FieldType.Short
                "Byte" -> FieldType.Byte
                "Double" -> FieldType.Double
                "Float" -> FieldType.Float
                "Boolean" -> FieldType.Boolean
                "Date", "LocalDate", "LocalDateTime", "ZonedDateTime" -> FieldType.Date
                "BigDecimal" -> FieldType.Double
                else -> {
                    // Skip collection types
                    if (isCollectionType(typeName)) {
                        return ProcessedFieldType(
                            elasticsearchType = FieldType.Object,
                            kotlinType = kotlinType,
                            kotlinTypeName = typeName,
                            isObjectType = false, // Don't generate ObjectFields for collections
                        )
                    }

                    // If it's a custom class (not enum), treat as object
                    val typeDeclaration = kotlinType.resolve().declaration
                    if (typeDeclaration is KSClassDeclaration &&
                        typeDeclaration.classKind == ClassKind.CLASS &&
                        !isStandardLibraryType(typeDeclaration.packageName.asString())
                    ) {
                        return ProcessedFieldType(
                            elasticsearchType = FieldType.Object,
                            kotlinType = kotlinType,
                            kotlinTypeName = getSimpleTypeName(kotlinType),
                            isObjectType = true,
                        )
                    }
                    FieldType.Keyword // Default fallback
                }
            }

        return ProcessedFieldType(
            elasticsearchType = elasticsearchType,
            kotlinType = kotlinType,
            kotlinTypeName = getSimpleTypeName(kotlinType),
            isObjectType = false,
        )
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

    private data class ObjectFieldInfo(
        val className: String,
        val packageName: String,
        val classDeclaration: KSClassDeclaration,
        val qualifiedName: String, // Add qualified name for proper identification
    )

    private fun determineTargetPackage(
        fieldType: ProcessedFieldType,
        currentPackage: String,
    ): String {
        // Check if we have a registered ObjectField for this type
        val objectFieldInfo = globalObjectFields[fieldType.kotlinTypeName]
        return objectFieldInfo?.packageName ?: currentPackage
    }

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
    private fun generateObjectFieldKey(classDeclaration: KSClassDeclaration): String {
        return classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString()
    }

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

    private fun collectObjectFields(documentClass: KSClassDeclaration) {
        documentClass.getAllProperties().forEach { property ->
            val fieldAnnotation = property.findAnnotation(Field::class)
            val fieldType = determineFieldType(property, fieldAnnotation, null)

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

                        if (hasField || hasId || hasMultiField) {
                            logger.info(
                                "Class $className has Elasticsearch annotation on property ${property.simpleName.asString()}: @Field=$hasField, @Id=$hasId, @MultiField=$hasMultiField",
                            )
                        }

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
                            qualifiedName = classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString(),
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
            val fieldType = determineFieldType(property, fieldAnnotation, null)

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
                                qualifiedName = nestedClass.qualifiedName?.asString() ?: nestedClass.simpleName.asString(),
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
        globalObjectFields.values.forEach { objectFieldInfo ->
            val fileSpec = generateObjectFieldsClassFromInfo(objectFieldInfo)
            writeGeneratedFile(fileSpec, objectFieldInfo.packageName, objectFieldInfo.className)
        }
    }

    private fun generateObjectFieldsClassFromInfo(objectFieldInfo: ObjectFieldInfo): FileSpec {
        val usedImports = mutableSetOf<String>()
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
                ).addModifiers() // Explicit empty modifiers to prevent default public
                .superclass(ClassName("com.qelasticsearch.dsl", "ObjectFields"))

        val processedPropertyNames = mutableSetOf<String>()
        objectFieldInfo.classDeclaration.getAllProperties().forEach { property ->
            val propertyName = property.simpleName.asString()
            if (propertyName !in processedPropertyNames) {
                processProperty(property, objectBuilder, objectFieldInfo.packageName, usedImports)
                processedPropertyNames.add(propertyName)
            }
        }

        val fileBuilder =
            FileSpec
                .builder(objectFieldInfo.packageName, objectFieldInfo.className)
                .addType(objectBuilder.build())
                .addImport("com.qelasticsearch.dsl", "ObjectFields")
                .indent("    ") // Use 4-space indentation for ktlint compliance

        // Add only the imports that are actually used
        usedImports.forEach { className ->
            fileBuilder.addImport("com.qelasticsearch.dsl", className)
        }

        return fileBuilder.build()
    }

    // Extension functions for KSP annotation handling
    private fun KSClassDeclaration.findAnnotation(annotationClass: kotlin.reflect.KClass<*>): KSAnnotation? =
        annotations.find {
            it.annotationType
                .resolve()
                .declaration.qualifiedName
                ?.asString() == annotationClass.qualifiedName
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

    private inline fun <reified T> KSAnnotation.getArgumentValue(name: String): T? =
        arguments.find { it.name?.asString() == name }?.value as? T
}
