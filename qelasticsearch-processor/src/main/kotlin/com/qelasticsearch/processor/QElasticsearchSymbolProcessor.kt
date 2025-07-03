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

            // Process each document class
            documentClasses.forEach { documentClass ->
                processDocumentClass(documentClass)
            }

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
        
        // Generate ObjectFields classes for nested objects
        val objectFieldsClasses = generateObjectFieldsClasses(documentClass, packageName)

        // Write the generated files
        writeGeneratedFile(qIndexClass, packageName, "Q$className")
        objectFieldsClasses.forEach { (name, fileSpec) ->
            writeGeneratedFile(fileSpec, packageName, name)
        }
    }

    private fun generateQIndexClass(
        documentClass: KSClassDeclaration,
        indexName: String,
        className: String,
        packageName: String
    ): FileSpec {
        val qIndexClassName = "Q$className"
        
        // Create the object declaration
        val objectBuilder = TypeSpec.objectBuilder(qIndexClassName)
            .superclass(ClassName("com.qelasticsearch.dsl", "Index"))
            .addSuperclassConstructorParameter("%S", indexName)

        // Process all properties in the document class, tracking names to avoid duplicates
        val processedPropertyNames = mutableSetOf<String>()
        documentClass.getAllProperties().forEach { property ->
            val propertyName = property.simpleName.asString()
            if (propertyName !in processedPropertyNames) {
                processProperty(property, objectBuilder, packageName)
                processedPropertyNames.add(propertyName)
            }
        }

        return FileSpec.builder(packageName, qIndexClassName)
            .addType(objectBuilder.build())
            .addImport("com.qelasticsearch.dsl", "Index")
            .addAnnotation(
                AnnotationSpec.builder(JvmName::class)
                    .addMember("%S", qIndexClassName)
                    .build()
            )
            .build()
    }

    private fun processProperty(property: KSPropertyDeclaration, objectBuilder: TypeSpec.Builder, packageName: String) {
        val fieldAnnotation = property.findAnnotation(Field::class)
        val multiFieldAnnotation = property.findAnnotation(MultiField::class)
        val idAnnotation = property.findAnnotation(Id::class)

        val propertyName = property.simpleName.asString()
        val fieldType = determineFieldType(property, fieldAnnotation, idAnnotation)

        when {
            multiFieldAnnotation != null -> {
                // Handle multi-field
                generateMultiFieldProperty(objectBuilder, propertyName, multiFieldAnnotation)
            }
            fieldType.isObjectType -> {
                // Handle object/nested field
                generateObjectFieldProperty(objectBuilder, propertyName, fieldType, packageName)
            }
            else -> {
                // Handle simple field
                generateSimpleFieldProperty(objectBuilder, propertyName, fieldType)
            }
        }
    }

    private fun generateSimpleFieldProperty(
        objectBuilder: TypeSpec.Builder,
        propertyName: String,
        fieldType: ProcessedFieldType
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
        packageName: String
    ) {
        val objectFieldsClassName = "Q${fieldType.kotlinTypeName}"
        val isNested = fieldType.elasticsearchType == FieldType.Nested

        val delegateCall = if (isNested) {
            "nestedField($objectFieldsClassName)"
        } else {
            "objectField($objectFieldsClassName)"
        }

        objectBuilder.addProperty(
            PropertySpec.builder(propertyName, ClassName(packageName, objectFieldsClassName))
                .delegate(delegateCall)
                .build()
        )
    }

    private fun generateMultiFieldProperty(
        objectBuilder: TypeSpec.Builder,
        propertyName: String,
        multiFieldAnnotation: KSAnnotation
    ) {
        val mainField = multiFieldAnnotation.getArgumentValue<KSAnnotation>("mainField")
        val innerFields = multiFieldAnnotation.getArgumentValue<List<KSAnnotation>>("otherFields") ?: emptyList()

        val mainFieldType = mainField?.getArgumentValue<FieldType>("type") ?: FieldType.Text
        val mainFieldDelegate = getFieldDelegate(mainFieldType)
        
        if (innerFields.isEmpty()) {
            // Simple multi-field without inner fields
            objectBuilder.addProperty(
                PropertySpec.builder(propertyName, ClassName("com.qelasticsearch.dsl", "Field"))
                    .delegate("$mainFieldDelegate")
                    .build()
            )
        } else {
            // Multi-field with inner fields - generate simpler syntax for now
            objectBuilder.addProperty(
                PropertySpec.builder(propertyName, ClassName("com.qelasticsearch.dsl", "Field"))
                    .delegate("$mainFieldDelegate")
                    .build()
            )
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

    private fun generateObjectFieldsClasses(
        documentClass: KSClassDeclaration,
        packageName: String
    ): Map<String, FileSpec> {
        val objectFieldsClasses = mutableMapOf<String, FileSpec>()

        documentClass.getAllProperties().forEach { property ->
            val fieldAnnotation = property.findAnnotation(Field::class)
            val fieldType = determineFieldType(property, fieldAnnotation, null)

            if (fieldType.isObjectType) {
                val objectFieldsClass = generateObjectFieldsClass(property, fieldType, packageName)
                objectFieldsClasses[objectFieldsClass.name] = objectFieldsClass
            }
        }

        return objectFieldsClasses
    }

    private fun generateObjectFieldsClass(
        property: KSPropertyDeclaration,
        fieldType: ProcessedFieldType,
        packageName: String
    ): FileSpec {
        val className = "Q${fieldType.kotlinTypeName}"
        
        val objectBuilder = TypeSpec.objectBuilder(className)
            .superclass(ClassName("com.qelasticsearch.dsl", "ObjectFields"))

        // For now, we'll create a placeholder ObjectFields class
        // In a full implementation, we would recursively process the nested class
        // TODO: Implement recursive processing of nested object fields

        return FileSpec.builder(packageName, className)
            .addType(objectBuilder.build())
            .addImport("com.qelasticsearch.dsl", "ObjectFields")
            .build()
    }

    private fun determineFieldType(
        property: KSPropertyDeclaration,
        fieldAnnotation: KSAnnotation?,
        idAnnotation: KSAnnotation?
    ): ProcessedFieldType {
        // If property has @Id annotation, treat as keyword by default
        if (idAnnotation != null) {
            return ProcessedFieldType(
                elasticsearchType = FieldType.Keyword,
                kotlinType = property.type,
                kotlinTypeName = getSimpleTypeName(property.type),
                isObjectType = false
            )
        }

        // If property has @Field annotation, use the specified type
        if (fieldAnnotation != null) {
            val fieldType = fieldAnnotation.getArgumentValue<FieldType>("type") ?: FieldType.Auto
            return ProcessedFieldType(
                elasticsearchType = fieldType,
                kotlinType = property.type,
                kotlinTypeName = getSimpleTypeName(property.type),
                isObjectType = fieldType == FieldType.Object || fieldType == FieldType.Nested
            )
        }

        // Auto-detect field type based on Kotlin type
        return autoDetectFieldType(property)
    }

    private fun autoDetectFieldType(property: KSPropertyDeclaration): ProcessedFieldType {
        val kotlinType = property.type
        val typeName = kotlinType.resolve().declaration.simpleName.asString()

        val elasticsearchType = when (typeName) {
            "String" -> FieldType.Text
            "Long" -> FieldType.Long
            "Int" -> FieldType.Integer
            "Short" -> FieldType.Short
            "Byte" -> FieldType.Byte
            "Double" -> FieldType.Double
            "Float" -> FieldType.Float
            "Boolean" -> FieldType.Boolean
            "Date", "LocalDate", "LocalDateTime" -> FieldType.Date
            else -> {
                // If it's a custom class, treat as object
                val typeDeclaration = kotlinType.resolve().declaration
                if (typeDeclaration is KSClassDeclaration && typeDeclaration.classKind == ClassKind.CLASS) {
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

    private fun getSimpleTypeName(type: KSTypeReference): String {
        return type.resolve().declaration.simpleName.asString()
    }

    private fun writeGeneratedFile(fileSpec: FileSpec, packageName: String, className: String) {
        try {
            val outputFile = codeGenerator.createNewFile(
                dependencies = Dependencies(false), // No specific dependencies for now
                packageName = packageName,
                fileName = className
            )
            
            outputFile.bufferedWriter().use { writer ->
                fileSpec.writeTo(writer)
            }
            
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

    private inline fun <reified T> KSAnnotation.getArgumentValue(name: String): T? {
        return arguments.find { it.name?.asString() == name }?.value as? T
    }
}