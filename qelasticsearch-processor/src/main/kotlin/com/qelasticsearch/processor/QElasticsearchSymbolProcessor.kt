package com.qelasticsearch.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.*
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*

/**
 * KSP-based annotation processor that generates type-safe Elasticsearch DSL classes
 * from Spring Data Elasticsearch @Document annotated classes.
 */
class QElasticsearchSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        try {
            // Find all classes annotated with @Document
            val documentClasses = resolver
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
        packageName: String
    ): FileSpec {
        val qIndexClassName = "Q$className"
        val usedImports = mutableSetOf<String>()
        
        // Create the object declaration
        val objectBuilder = TypeSpec.objectBuilder(qIndexClassName)
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

        val fileBuilder = FileSpec.builder(packageName, qIndexClassName)
            .addType(objectBuilder.build())
            .addImport("com.qelasticsearch.dsl", "Index")
            .addAnnotation(
                AnnotationSpec.builder(JvmName::class)
                    .addMember("%S", qIndexClassName)
                    .build()
            )
            
        // Add only the imports that are actually used
        usedImports.forEach { className ->
            fileBuilder.addImport("com.qelasticsearch.dsl", className)
        }
            
        return fileBuilder.build()
    }

    private fun processProperty(property: KSPropertyDeclaration, objectBuilder: TypeSpec.Builder, packageName: String, usedImports: MutableSet<String> = mutableSetOf()) {
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
                generateObjectFieldProperty(objectBuilder, propertyName, fieldType, packageName, usedImports)
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
        usedImports: MutableSet<String> = mutableSetOf()
    ) {
        val delegateCall = when (fieldType.elasticsearchType) {
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
            FieldType.Object -> {
                logger.info("Object field '$propertyName' of type '${fieldType.kotlinTypeName}' has no QObjectField, using UnknownObjectFields")
                return generateUnknownObjectFieldProperty(objectBuilder, propertyName, fieldType, false, usedImports)
            }
            FieldType.Nested -> {
                logger.info("Nested field '$propertyName' of type '${fieldType.kotlinTypeName}' has no QObjectField, using UnknownNestedFields")
                return generateUnknownObjectFieldProperty(objectBuilder, propertyName, fieldType, true, usedImports)
            }
            else -> "keyword()" // Default fallback
        }

        objectBuilder.addProperty(
            PropertySpec.builder(propertyName, ClassName("com.qelasticsearch.dsl", "Field"))
                .delegate("$delegateCall")
                .build()
        )
    }

    private fun generateObjectFieldProperty(
        objectBuilder: TypeSpec.Builder,
        propertyName: String,
        fieldType: ProcessedFieldType,
        packageName: String,
        usedImports: MutableSet<String> = mutableSetOf()
    ) {
        val objectFieldsClassName = "Q${fieldType.kotlinTypeName}"
        val isNested = fieldType.elasticsearchType == FieldType.Nested

        val delegateCall = if (isNested) {
            "nestedField($objectFieldsClassName)"
        } else {
            "objectField($objectFieldsClassName)"
        }

        // Use the actual package where the Q-class should be located
        val targetPackage = determineTargetPackage(fieldType, packageName)

        objectBuilder.addProperty(
            PropertySpec.builder(propertyName, ClassName(targetPackage, objectFieldsClassName))
                .delegate(delegateCall)
                .build()
        )
    }
    
    private fun generateUnknownObjectFieldProperty(
        objectBuilder: TypeSpec.Builder,
        propertyName: String,
        fieldType: ProcessedFieldType,
        isNested: Boolean,
        usedImports: MutableSet<String> = mutableSetOf()
    ) {
        val className = if (isNested) "UnknownNestedFields" else "UnknownObjectFields"
        val delegateCall = if (isNested) {
            "nestedField($className)"
        } else {
            "objectField($className)"
        }
        
        usedImports.add(className)

        objectBuilder.addProperty(
            PropertySpec.builder(propertyName, ClassName("com.qelasticsearch.dsl", className))
                .delegate(delegateCall)
                .build()
        )
    }

    private fun generateMultiFieldProperty(
        objectBuilder: TypeSpec.Builder,
        propertyName: String,
        multiFieldAnnotation: KSAnnotation,
        usedImports: MutableSet<String> = mutableSetOf()
    ) {
        val mainField = multiFieldAnnotation.getArgumentValue<KSAnnotation>("mainField")
        val innerFields = multiFieldAnnotation.getArgumentValue<List<KSAnnotation>>("otherFields") ?: emptyList()

        val mainFieldType = extractFieldTypeFromAnnotation(mainField)
        val mainFieldDelegate = getFieldDelegate(mainFieldType)
        
        if (innerFields.isEmpty()) {
            // Simple multi-field without inner fields - use regular field
            objectBuilder.addProperty(
                PropertySpec.builder(propertyName, ClassName("com.qelasticsearch.dsl", "Field"))
                    .delegate("$mainFieldDelegate")
                    .build()
            )
        } else {
            // Multi-field with inner fields - generate MultiFieldProxy
            val mainFieldClass = getFieldClass(mainFieldType)
            usedImports.add(mainFieldClass)
            usedImports.add("MultiFieldProxy")
            
            val innerFieldsCode = buildString {
                innerFields.forEach { innerFieldAnnotation ->
                    val suffix = innerFieldAnnotation.getArgumentValue<String>("suffix") ?: "unknown"
                    val innerFieldType = extractFieldTypeFromAnnotation(innerFieldAnnotation)
                    val innerFieldClass = getFieldClass(innerFieldType)
                    usedImports.add(innerFieldClass)
                    append("field(\"$suffix\") { $innerFieldClass(\"$suffix\") }; ")
                }
            }
            
            objectBuilder.addProperty(
                PropertySpec.builder(propertyName, ClassName("com.qelasticsearch.dsl", "MultiFieldProxy"))
                    .delegate("multiFieldProxy($mainFieldClass(\"$propertyName\")) { $innerFieldsCode }")
                    .build()
            )
        }
    }

    private fun extractFieldTypeFromAnnotation(annotation: KSAnnotation?): FieldType {
        return if (annotation != null) {
            extractFieldType(annotation) ?: FieldType.Text
        } else {
            FieldType.Text
        }
    }

    private fun getFieldDelegate(fieldType: FieldType): String {
        return when (fieldType) {
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
    }

    private fun getFieldClass(fieldType: FieldType): String {
        return when (fieldType) {
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
    }


    private fun determineFieldType(
        property: KSPropertyDeclaration,
        fieldAnnotation: KSAnnotation?,
        idAnnotation: KSAnnotation?
    ): ProcessedFieldType {
        val propertyName = property.simpleName.asString()
        
        // If property has @Id annotation, treat as keyword by default
        if (idAnnotation != null) {
            logger.info("Property $propertyName has @Id annotation, using Keyword type")
            return ProcessedFieldType(
                elasticsearchType = FieldType.Keyword,
                kotlinType = property.type,
                kotlinTypeName = getSimpleTypeName(property.type),
                isObjectType = false
            )
        }

        // If property has @Field annotation, use the specified type
        if (fieldAnnotation != null) {
            val fieldType = extractFieldType(fieldAnnotation) ?: FieldType.Auto
            logger.info("Property $propertyName has @Field annotation with type: $fieldType")
            
            // Handle nested/object types - including collections
            val isObjectType = when (fieldType) {
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
                            logger.info("Checking type $typeName: classKind=${typeDeclaration.classKind}, package=${typeDeclaration.packageName.asString()}")
                            if (typeName == "EventTarget") {
                                logger.warn("Found EventTarget: classKind=${typeDeclaration.classKind}, isEnum=${typeDeclaration.classKind == ClassKind.ENUM_CLASS}")
                            }
                        }
                        typeDeclaration is KSClassDeclaration && 
                        typeDeclaration.classKind == ClassKind.CLASS &&
                        !isStandardLibraryType(typeDeclaration.packageName.asString())
                    }
                }
                else -> false
            }
            
            val kotlinTypeName = if (isCollectionType(getSimpleTypeName(property.type))) {
                getCollectionElementType(property)?.simpleName?.asString() ?: getSimpleTypeName(property.type)
            } else {
                getSimpleTypeName(property.type)
            }
            
            return ProcessedFieldType(
                elasticsearchType = fieldType,
                kotlinType = property.type,
                kotlinTypeName = kotlinTypeName,
                isObjectType = isObjectType
            )
        }

        // Auto-detect field type based on Kotlin type
        logger.info("Property $propertyName has no annotations, auto-detecting type")
        return autoDetectFieldType(property)
    }

    private fun autoDetectFieldType(property: KSPropertyDeclaration): ProcessedFieldType {
        val kotlinType = property.type
        val typeName = kotlinType.resolve().declaration.simpleName.asString()

        val elasticsearchType = when (typeName) {
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
                        isObjectType = false // Don't generate ObjectFields for collections
                    )
                }
                
                // If it's a custom class (not enum), treat as object
                val typeDeclaration = kotlinType.resolve().declaration
                if (typeDeclaration is KSClassDeclaration && 
                    typeDeclaration.classKind == ClassKind.CLASS &&
                    !isStandardLibraryType(typeDeclaration.packageName.asString())) {
                    return ProcessedFieldType(
                        elasticsearchType = FieldType.Object,
                        kotlinType = kotlinType,
                        kotlinTypeName = getSimpleTypeName(kotlinType),
                        isObjectType = true
                    )
                }
                FieldType.Keyword // Default fallback
            }
        }

        return ProcessedFieldType(
            elasticsearchType = elasticsearchType,
            kotlinType = kotlinType,
            kotlinTypeName = getSimpleTypeName(kotlinType),
            isObjectType = false
        )
    }

    private fun isCollectionType(typeName: String): Boolean {
        return typeName in setOf(
            "List", "MutableList", "ArrayList", "LinkedList",
            "Set", "MutableSet", "HashSet", "LinkedHashSet",
            "Collection", "MutableCollection",
            "Array", "Map", "MutableMap", "HashMap", "LinkedHashMap"
        )
    }

    private fun isStandardLibraryType(packageName: String): Boolean {
        return packageName.startsWith("kotlin.") || 
               packageName.startsWith("java.") ||
               packageName.startsWith("javax.") ||
               packageName == "kotlin" ||
               packageName == "java"
    }

    private fun findNestedClass(property: KSPropertyDeclaration, fieldType: ProcessedFieldType): KSClassDeclaration? {
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

    private fun getSimpleTypeName(type: KSTypeReference): String {
        return type.resolve().declaration.simpleName.asString()
    }
    
    private fun getCollectionElementType(property: KSPropertyDeclaration): KSClassDeclaration? {
        return try {
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
    }

    private val generatedFiles = mutableSetOf<String>()
    private val globalObjectFields = mutableMapOf<String, ObjectFieldInfo>()

    private fun writeGeneratedFile(fileSpec: FileSpec, packageName: String, className: String) {
        try {
            val fileKey = "$packageName.$className"
            if (fileKey in generatedFiles) {
                logger.info("Skipping duplicate file generation for: $fileKey")
                return
            }
            
            val outputFile = codeGenerator.createNewFile(
                dependencies = Dependencies(false), // No specific dependencies for now
                packageName = packageName,
                fileName = className
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
        val isObjectType: Boolean
    )

    private data class ObjectFieldInfo(
        val className: String,
        val packageName: String,
        val classDeclaration: KSClassDeclaration
    )

    private fun determineTargetPackage(fieldType: ProcessedFieldType, currentPackage: String): String {
        // Check if we have a registered ObjectField for this type
        val objectFieldInfo = globalObjectFields[fieldType.kotlinTypeName]
        return objectFieldInfo?.packageName ?: currentPackage
    }

    private fun collectObjectFields(documentClass: KSClassDeclaration) {
        val packageName = documentClass.packageName.asString()
        
        documentClass.getAllProperties().forEach { property ->
            val fieldAnnotation = property.findAnnotation(Field::class)
            val fieldType = determineFieldType(property, fieldAnnotation, null)
            
            if (fieldType.isObjectType) {
                // For collections, get the element type directly
                val nestedClass = if (isCollectionType(getSimpleTypeName(property.type))) {
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
                    
                    val className = "Q${fieldType.kotlinTypeName}"
                    
                    globalObjectFields[fieldType.kotlinTypeName] = ObjectFieldInfo(
                        className = className,
                        packageName = targetPackage,
                        classDeclaration = nestedClass
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
                
                val hasElasticsearchAnnotations = classDeclaration.getAllProperties().any { property ->
                    val hasField = property.findAnnotation(Field::class) != null
                    val hasId = property.findAnnotation(Id::class) != null
                    val hasMultiField = property.findAnnotation(MultiField::class) != null
                    
                    if (hasField || hasId || hasMultiField) {
                        logger.info("Class $className has Elasticsearch annotation on property ${property.simpleName.asString()}: @Field=$hasField, @Id=$hasId, @MultiField=$hasMultiField")
                    }
                    
                    hasField || hasId || hasMultiField
                }
                
                val shouldRegister = when {
                    className in globalObjectFields -> {
                        logger.info("Class $className already registered, skipping")
                        false
                    }
                    hasElasticsearchAnnotations -> {
                        logger.info("Class $className has Elasticsearch annotations, will register")
                        true
                    }
                    else -> {
                        // Check specifically for the missing classes
                        if (className == "IndexMandateOperationState" || className == "EventTarget") {
                            logger.warn("MISSING CLASS: $className in $packageName - no Elasticsearch annotations found")
                            classDeclaration.getAllProperties().forEach { prop ->
                                logger.warn("  Property ${prop.simpleName.asString()}: ${prop.annotations.map { it.annotationType.resolve().declaration.simpleName.asString() }}")
                            }
                        }
                        false
                    }
                }
                
                if (shouldRegister) {
                    globalObjectFields[className] = ObjectFieldInfo(
                        className = "Q$className",
                        packageName = packageName,
                        classDeclaration = classDeclaration
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
    
    private fun collectNestedClasses(parentClass: KSClassDeclaration, allClasses: MutableList<KSClassDeclaration>) {
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
                    
                    val className = "Q${fieldType.kotlinTypeName}"
                    
                    if (fieldType.kotlinTypeName !in globalObjectFields) {
                        globalObjectFields[fieldType.kotlinTypeName] = ObjectFieldInfo(
                            className = className,
                            packageName = targetPackage,
                            classDeclaration = nestedClass
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
        val objectBuilder = TypeSpec.objectBuilder(objectFieldInfo.className)
            .superclass(ClassName("com.qelasticsearch.dsl", "ObjectFields"))

        val processedPropertyNames = mutableSetOf<String>()
        objectFieldInfo.classDeclaration.getAllProperties().forEach { property ->
            val propertyName = property.simpleName.asString()
            if (propertyName !in processedPropertyNames) {
                processProperty(property, objectBuilder, objectFieldInfo.packageName, usedImports)
                processedPropertyNames.add(propertyName)
            }
        }

        val fileBuilder = FileSpec.builder(objectFieldInfo.packageName, objectFieldInfo.className)
            .addType(objectBuilder.build())
            .addImport("com.qelasticsearch.dsl", "ObjectFields")
            
        // Add only the imports that are actually used
        usedImports.forEach { className ->
            fileBuilder.addImport("com.qelasticsearch.dsl", className)
        }
        
        return fileBuilder.build()
    }

    // Extension functions for KSP annotation handling
    private fun KSClassDeclaration.findAnnotation(annotationClass: kotlin.reflect.KClass<*>): KSAnnotation? {
        return annotations.find { 
            it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationClass.qualifiedName 
        }
    }

    private fun KSPropertyDeclaration.findAnnotation(annotationClass: kotlin.reflect.KClass<*>): KSAnnotation? {
        return annotations.find { 
            it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationClass.qualifiedName 
        }
    }

    private fun extractFieldType(fieldAnnotation: KSAnnotation): FieldType? {
        return try {
            val typeArgument = fieldAnnotation.arguments.find { it.name?.asString() == "type" }
            val value = typeArgument?.value
            
            logger.info("Extracting FieldType from argument: $value (type: ${value?.javaClass?.simpleName})")
            
            when (value) {
                is KSType -> {
                    // Handle enum reference - this is usually how enums are represented in KSP
                    val enumName = value.declaration.simpleName.asString()
                    logger.info("Found KSType enum value: $enumName")
                    FieldType.values().find { it.name == enumName }
                }
                is String -> {
                    // Handle string representation
                    logger.info("Found String enum value: $value")
                    FieldType.values().find { it.name == value }
                }
                // Handle KSP's representation of enum constants
                else -> {
                    // Try to get the enum value by looking at the qualified name
                    val valueStr = value.toString()
                    logger.info("Trying to parse enum from string representation: $valueStr")
                    
                    // Extract enum name from patterns like "FieldType.Text" or just "Text"
                    val enumName = when {
                        valueStr.contains("FieldType.") -> valueStr.substringAfter("FieldType.")
                        valueStr.contains(".") -> valueStr.substringAfterLast(".")
                        else -> valueStr
                    }
                    
                    logger.info("Extracted enum name: $enumName")
                    FieldType.values().find { it.name == enumName }
                }
            }
        } catch (e: Exception) {
            logger.error("Error extracting FieldType: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private inline fun <reified T> KSAnnotation.getArgumentValue(name: String): T? {
        return arguments.find { it.name?.asString() == name }?.value as? T
    }
}