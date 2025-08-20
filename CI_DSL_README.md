# GitLab CI Kotlin DSL

This project uses Kotlin DSL for GitLab CI configuration to improve maintainability and provide type safety.

## Source of Truth

- **`.gitlab-ci.main.kts`** - Kotlin DSL source file (this is what you should edit)
- **`.gitlab-ci.yml`** - Generated YAML file (committed for GitLab compatibility)

## How It Works

This project uses a **parent/child pipeline architecture**:

1. **Parent Pipeline** (`.gitlab-ci.yml`): 
   - `prepare` stage: Generates child pipeline YAML from Kotlin DSL
   - `run` stage: Triggers the generated child pipeline

2. **Child Pipeline** (generated from `.gitlab-ci.main.kts`):
   - `build` → `test` → `publish` stages
   - Contains the actual CI/CD logic

## Making Changes

1. **Edit the Kotlin DSL**: Modify `.gitlab-ci.main.kts` 
2. **Commit and push**: GitLab will automatically generate and run the child pipeline
3. **No manual YAML generation needed**: GitLab handles everything

The parent pipeline generates `.gitlab-ci-generated.yml` on-the-fly and triggers it as a child pipeline.

## Benefits

✅ **Type Safety**: Catch errors at compile time, not runtime  
✅ **IDE Support**: Full autocomplete, refactoring, and navigation  
✅ **Better Readability**: Clear, structured code vs YAML indentation  
✅ **Version Control**: Easier to track meaningful changes  
✅ **Maintainability**: Easier to extend and modify complex pipelines  

## Pipeline Architecture

### Parent Pipeline (.gitlab-ci.yml)
```yaml
stages: prepare → run
```
- **prepare**: Execute Kotlin DSL to generate child pipeline YAML
- **run**: Trigger child pipeline with `strategy: depend`

### Child Pipeline (generated)
```kotlin
stages: build → test → publish
```
- **build**: Compile code with `./gradlew build -x test`, cache dependencies
- **test**: Run full test suite with `./gradlew test`, JUnit reports
- **publish**: Automatic publishing with dynamic job names
- **publish-manual**: Manual publishing with version info in job name

## Publishing Logic

| Trigger | Behavior |
|---------|----------|
| **Master branch** | ✅ Automatic publish |
| **Git tags** | ✅ Automatic versioned publish |
| **Merge requests** | 🔘 Manual publish available |
| **Feature branches** | 🔘 Manual publish available |

## Environment Variables

- `GRADLE_OPTS`: JVM options for Gradle (`-Dorg.gradle.daemon=false`)
- `GRADLE_USER_HOME`: Gradle cache directory (`$CI_PROJECT_DIR/.gradle`)
- `generatedFile`: Generated child pipeline YAML file (`.gitlab-ci-generated.yml`)

## Cache Strategy

Global caching of:
- `.gradle/wrapper` - Gradle wrapper distribution
- `.gradle/caches` - Dependencies and build outputs

## Dependencies

The Kotlin DSL uses:
- `com.github.pcimcioch:gitlab-ci-kotlin-dsl:1.7.0`

## Dynamic Job Names

Job names automatically show the artifact version:
- **`publish (1.0-SNAPSHOT)`** - Shows actual published version
- **`publish-manual (1.0-SNAPSHOT)`** - Manual publish with version info
- **`publish (v1.2.3)`** - Release versions from git tags

## Example Usage

```kotlin
// Essential workflow rules for child pipeline
workflow {
    rules {
        rule {
            ifCondition = "\$CI_PIPELINE_SOURCE == \"parent_pipeline\""
        }
    }
}

job("test") {
    stage(Stages.test)
    
    script {
        +"./gradlew test"
    }
    
    artifacts {
        whenUpload = WhenUploadType.ALWAYS
        reports {
            junit("*/build/test-results/test/TEST-*.xml")
        }
    }
}
```

## Troubleshooting

**Script won't run?**
```bash
chmod +x .gitlab-ci.main.kts
```

**YAML format issues?**
- The DSL generates valid YAML but with quoted keys
- Manually format the output for better GitLab compatibility

**Want to test changes locally?**
```bash
./.gitlab-ci.main.kts | head -20  # Preview first 20 lines
```

**Pipeline not triggering?**
- Check workflow rules in `.gitlab-ci.main.kts`
- Ensure `$CI_PIPELINE_SOURCE == "parent_pipeline"` rule exists
- Verify parent pipeline generates the child pipeline file

**Empty child pipeline error?**
- Missing workflow rules in Kotlin DSL
- Child pipeline needs `workflow.rules` for `parent_pipeline` source