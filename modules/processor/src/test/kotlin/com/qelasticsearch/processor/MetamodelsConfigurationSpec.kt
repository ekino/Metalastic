package com.qelasticsearch.processor

import com.qelasticsearch.processor.model.ElasticsearchGraph
import com.qelasticsearch.processor.options.MetamodelsConfiguration
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class MetamodelsConfigurationSpec :
  ShouldSpec({
    should("use default package when no overrides provided") {
      val config = MetamodelsConfiguration()
      val mockGraph = mockk<ElasticsearchGraph>()
      val mockModel = mockk<ElasticsearchGraph.QClassModel>()
      val mockDeclaration = mockk<com.google.devtools.ksp.symbol.KSClassDeclaration>()
      val mockFile = mockk<com.google.devtools.ksp.symbol.KSFile>()

      every { mockGraph.models() } returns setOf(mockModel)
      every { mockGraph.rootModels() } returns sequenceOf(mockModel)
      every { mockModel.sourceClassDeclaration } returns mockDeclaration
      every { mockModel.packageName } returns "com.example"
      every { mockDeclaration.containingFile } returns mockFile
      every { mockFile.filePath } returns "/src/main/java/com/example/Test.java"

      val result = config.generateMetamodelsInfo(mockGraph)

      result.packageName shouldBe "com.example"
      result.className shouldBe CoreConstants.Metamodels.SIMPLE_NAME
    }

    should("apply global package override") {
      val config = MetamodelsConfiguration(packageOverride = "com.custom.global")
      val mockGraph = mockk<ElasticsearchGraph>()
      val mockModel = mockk<ElasticsearchGraph.QClassModel>()
      val mockDeclaration = mockk<com.google.devtools.ksp.symbol.KSClassDeclaration>()
      val mockFile = mockk<com.google.devtools.ksp.symbol.KSFile>()

      every { mockGraph.models() } returns setOf(mockModel)
      every { mockModel.sourceClassDeclaration } returns mockDeclaration
      every { mockDeclaration.containingFile } returns mockFile
      every { mockFile.filePath } returns "/src/main/java/com/example/Test.java"

      val result = config.generateMetamodelsInfo(mockGraph)

      result.packageName shouldBe "com.custom.global"
      result.className shouldBe CoreConstants.Metamodels.SIMPLE_NAME
    }

    should("apply source set specific package override") {
      val config =
        MetamodelsConfiguration(sourceSetPackageOverrides = mapOf("main" to "com.custom.main"))
      val mockGraph = mockk<ElasticsearchGraph>()
      val mockModel = mockk<ElasticsearchGraph.QClassModel>()
      val mockDeclaration = mockk<com.google.devtools.ksp.symbol.KSClassDeclaration>()
      val mockFile = mockk<com.google.devtools.ksp.symbol.KSFile>()

      every { mockGraph.models() } returns setOf(mockModel)
      every { mockModel.sourceClassDeclaration } returns mockDeclaration
      every { mockDeclaration.containingFile } returns mockFile
      every { mockFile.filePath } returns "/src/main/java/com/example/Test.java"

      val result = config.generateMetamodelsInfo(mockGraph)

      result.packageName shouldBe "com.custom.main"
      result.className shouldBe CoreConstants.Metamodels.SIMPLE_NAME
    }

    should("apply global class name override") {
      val config = MetamodelsConfiguration(classNameOverride = "CustomMetamodels")
      val mockGraph = mockk<ElasticsearchGraph>()
      val mockModel = mockk<ElasticsearchGraph.QClassModel>()
      val mockDeclaration = mockk<com.google.devtools.ksp.symbol.KSClassDeclaration>()
      val mockFile = mockk<com.google.devtools.ksp.symbol.KSFile>()

      every { mockGraph.models() } returns setOf(mockModel)
      every { mockGraph.rootModels() } returns sequenceOf(mockModel)
      every { mockModel.sourceClassDeclaration } returns mockDeclaration
      every { mockModel.packageName } returns "com.example"
      every { mockDeclaration.containingFile } returns mockFile
      every { mockFile.filePath } returns "/src/main/java/com/example/Test.java"

      val result = config.generateMetamodelsInfo(mockGraph)

      result.packageName shouldBe "com.example"
      result.className shouldBe "CustomMetamodels"
    }

    should("find common ancestor package for multiple packages") {
      val config = MetamodelsConfiguration()
      val mockGraph = mockk<ElasticsearchGraph>()
      val mockModel1 = mockk<ElasticsearchGraph.QClassModel>()
      val mockModel2 = mockk<ElasticsearchGraph.QClassModel>()
      val mockDeclaration1 = mockk<com.google.devtools.ksp.symbol.KSClassDeclaration>()
      val mockFile = mockk<com.google.devtools.ksp.symbol.KSFile>()

      every { mockGraph.models() } returns setOf(mockModel1)
      every { mockGraph.rootModels() } returns sequenceOf(mockModel1, mockModel2)
      every { mockModel1.sourceClassDeclaration } returns mockDeclaration1
      every { mockModel1.packageName } returns "com.example.test"
      every { mockModel2.packageName } returns "com.example.other"
      every { mockDeclaration1.containingFile } returns mockFile
      every { mockFile.filePath } returns "/src/main/java/com/example/test/Test.java"

      val result = config.generateMetamodelsInfo(mockGraph)

      result.packageName shouldBe "com.example"
      result.className shouldBe CoreConstants.Metamodels.SIMPLE_NAME
    }
  })
