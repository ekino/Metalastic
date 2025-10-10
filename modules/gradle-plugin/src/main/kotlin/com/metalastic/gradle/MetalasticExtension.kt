package com.metalastic.gradle

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

/**
 * Root extension for configuring Metalastic with a type-safe DSL.
 *
 * Usage:
 * ```kotlin
 * metalastic {
 *     metamodels {
 *         main {
 *             packageName = "com.example.search.metamodels"
 *             className = "MainMetamodels"
 *         }
 *         test {
 *             packageName = "com.example.test.metamodels"
 *             className = "TestMetamodels"
 *         }
 *         fallbackPackage = "com.example.metamodels"
 *     }
 *
 *     features {
 *         generateJavaCompatibility = true
 *         generatePrivateClassMetamodels = false
 *     }
 *
 *     dsl {
 *         strictMode = true
 *     }
 *
 *     reporting {
 *         enabled = true
 *         outputPath = "build/reports/metalastic/processor-report.md"
 *     }
 * }
 * ```
 */
abstract class MetalasticExtension @Inject constructor(objects: ObjectFactory) {

  /** Configuration for metamodel generation */
  val metamodels: MetamodelsConfiguration = objects.newInstance(MetamodelsConfiguration::class.java)

  /** Configuration for feature toggles */
  val features: FeaturesConfiguration = objects.newInstance(FeaturesConfiguration::class.java)

  /** Configuration for elasticsearch-dsl module */
  val dsl: DslConfiguration = objects.newInstance(DslConfiguration::class.java)

  /** Configuration for debug reporting */
  val reporting: ReportingConfiguration = objects.newInstance(ReportingConfiguration::class.java)

  /** Configure metamodel generation */
  fun metamodels(action: Action<MetamodelsConfiguration>) {
    action.execute(metamodels)
  }

  /** Configure feature toggles */
  fun features(action: Action<FeaturesConfiguration>) {
    action.execute(features)
  }

  /** Configure elasticsearch-dsl module */
  fun dsl(action: Action<DslConfiguration>) {
    action.execute(dsl)
  }

  /** Configure debug reporting */
  fun reporting(action: Action<ReportingConfiguration>) {
    action.execute(reporting)
  }
}
