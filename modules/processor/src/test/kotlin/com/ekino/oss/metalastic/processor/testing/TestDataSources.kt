/*
 * Copyright (c) 2025 ekino (https://www.ekino.com/)
 */
package com.ekino.oss.metalastic.processor.testing

import com.tschuchort.compiletesting.SourceFile
import java.io.File
import org.springframework.data.elasticsearch.annotations.FieldType

/**
 * Common test data sources for processor testing.
 *
 * Provides reusable source files for different test scenarios.
 */
object TestDataSources {

  val kotlinDataSetDirectory = File("src/test/kotlin/com/example/dataset")
  val javaDataSetDirectory = File("src/test/java/com/example/dataset")

  /**
   * Loads a source file from disk and re-emits it through [SourceFile.kotlin] / [SourceFile.java]
   * with a name that includes the package directory layout. The package layout is required so that
   * javac (and KSP2) finds Java sources under their declared package; the source root override in
   * [KspTestUtils] points at the temp sources root so all package-qualified files resolve.
   *
   * `SourceFile.fromPath` is deprecated upstream as unreliable with KSP.
   */
  private val packageRegex = Regex("""^\s*package\s+([\w.]+)\s*;?""", RegexOption.MULTILINE)

  private fun fromPath(file: File): SourceFile {
    val content = file.readText()
    val packagePath =
      packageRegex.find(content)?.groupValues?.get(1)?.replace('.', '/')?.let { "$it/" }.orEmpty()
    val qualifiedName = "$packagePath${file.name}"
    return when (file.extension) {
      "kt" -> SourceFile.kotlin(qualifiedName, content)
      "java" -> SourceFile.java(qualifiedName, content)
      else -> error("Unsupported source extension for ${file.path}")
    }
  }

  /** Simple document with basic field types */
  fun simpleDocumentWithAllFieldTypeExceptObjectAndNested(): SourceFile =
    SourceFile.kotlin(
      "SimpleDocument.kt",
      buildString {
        append(
          """
          package com.example.test

          import org.springframework.data.elasticsearch.annotations.Document
          import org.springframework.data.elasticsearch.annotations.Field
          import org.springframework.data.elasticsearch.annotations.FieldType

          @Document(indexName = "simple")
          data class SimpleDocument(
          """
            .trimIndent()
        )
        FieldType.entries
          .filterNot { it == FieldType.Object || it == FieldType.Nested }
          .forEach { fieldType ->
            append("\n    @Field(type = FieldType.${fieldType.name})")
            append("\n    val field${fieldType.name}: String,")
          }
        append("\n)\n")
      },
    )

  /** Document with MultiField annotations */
  fun multiFieldDocument(): SourceFile =
    SourceFile.kotlin(
      "MultiFieldDocument.kt",
      """
      package com.example.test

      import org.springframework.data.elasticsearch.annotations.Document
              import org.springframework.data.elasticsearch.annotations.Field
              import org.springframework.data.elasticsearch.annotations.FieldType
              import org.springframework.data.elasticsearch.annotations.MultiField
              import org.springframework.data.elasticsearch.annotations.InnerField

              @Document(indexName = "multifield")
              data class MultiFieldDocument(
                  @Field(type = FieldType.Keyword)
                  val id: String,

                  @MultiField(
                      mainField = Field(type = FieldType.Text),
                      otherFields = [
                          InnerField(suffix = "keyword", type = FieldType.Keyword),
                          InnerField(suffix = "search", type = FieldType.Text)
                      ]
                  )
                  val multiName: String,

                  @MultiField(
                      mainField = Field(type = FieldType.Long),
                      otherFields = [
                          InnerField(suffix = "text", type = FieldType.Text)
                      ]
                  )
                  val multiCode: Long
              )
      """
        .trimIndent(),
    )

  /** Complex document with multiple nested levels */
  fun complexDocument(): SourceFile = fromPath(File(kotlinDataSetDirectory, "ComplexDocument.kt"))

  /** Document with inner/nested class also annotated as @Document */
  fun outerDocument(): SourceFile = fromPath(File(kotlinDataSetDirectory, "OuterDocument.kt"))

  fun testDocument(): SourceFile = fromPath(File(kotlinDataSetDirectory, "TestDocument.kt"))

  fun simpleDocument(): SourceFile = fromPath(File(kotlinDataSetDirectory, "SimpleDocument.kt"))

  fun objectClass(): SourceFile = fromPath(File(kotlinDataSetDirectory, "ObjectClass.kt"))

  fun otherObjectClass(): SourceFile = fromPath(File(kotlinDataSetDirectory, "OtherObjectClass.kt"))

  fun javaTestDocument(): SourceFile = fromPath(File(javaDataSetDirectory, "JavaTestDocument.java"))

  fun javaRecordDocument(): SourceFile =
    fromPath(File(javaDataSetDirectory, "JavaRecordDocument.java"))

  fun javaRecordAddress(): SourceFile =
    fromPath(File(javaDataSetDirectory, "JavaRecordAddress.java"))

  fun javaRecordTag(): SourceFile = fromPath(File(javaDataSetDirectory, "JavaRecordTag.java"))

  fun recordWithInterface(): SourceFile =
    fromPath(File(javaDataSetDirectory, "RecordWithInterface.java"))

  fun identifiableInterface(): SourceFile =
    fromPath(File(javaDataSetDirectory, "Identifiable.java"))

  fun exampleDocument(): SourceFile = fromPath(File(javaDataSetDirectory, "ExampleDocument.java"))

  fun nameCollision(): SourceFile = fromPath(File(javaDataSetDirectory, "NameCollision.java"))

  fun indexPerson(): SourceFile = fromPath(File(javaDataSetDirectory, "IndexPerson.java"))

  /** Document with Java compatibility (uses Java types) */
  fun javaCompatDocument(): SourceFile =
    SourceFile.kotlin(
      "JavaCompatDocument.kt",
      """
      package com.example.test

      import org.springframework.data.elasticsearch.annotations.Document
              import org.springframework.dat.elasticsearch.annotations.Field
              import org.springframework.dat.elasticsearch.annotations.FieldType
              import java.util.Dat
              import java.util.Lis

              @Document(indexName = "java-compat")
              data class JavaCompatDocument(
                  @Field(type = FieldType.Keyword)
                  val id: String,

                  @Field(type = FieldType.Text)
                  val description: String,

                  @Field(type = FieldType.Date)
                  val createdDate: Date,

                  @Field(type = FieldType.Nested)
                  val tags: List<Tag>
              ) {
                  data class Tag(
                      @Field(type = FieldType.Keyword)
                      val name: String,

                      @Field(type = FieldType.Integer)
                      val weight: Int
                  )
              }
      """
        .trimIndent(),
    )
}
