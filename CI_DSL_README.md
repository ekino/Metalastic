# GitLab CI Kotlin DSL

This project uses Kotlin DSL for GitLab CI configuration to improve maintainability and provide type safety.

## Source of Truth

- **`.gitlab-ci.main.kts`** - Kotlin DSL source file (this is what you should edit)
- **`.gitlab-ci.yml`** - Generated YAML file (committed for GitLab compatibility)

## Making Changes

1. **Edit the Kotlin DSL**: Modify `.gitlab-ci.main.kts`
2. **Generate YAML**: Run `./.gitlab-ci.main.kts > .gitlab-ci.yml`
3. **Commit both files**: The DSL source and the generated YAML

The `generatedFile` variable in the YAML indicates this file is generated from the Kotlin DSL.

## Benefits

✅ **Type Safety**: Catch errors at compile time, not runtime  
✅ **IDE Support**: Full autocomplete, refactoring, and navigation  
✅ **Better Readability**: Clear, structured code vs YAML indentation  
✅ **Version Control**: Easier to track meaningful changes  
✅ **Maintainability**: Easier to extend and modify complex pipelines  

## Pipeline Structure

```kotlin
stages: build → test → publish → manual_publish
```

### Stage Details

- **build**: Compile code, skip tests, cache dependencies
- **test**: Run full test suite with JUnit reports  
- **publish**: Automatic publishing (master branch + git tags)
- **manual_publish**: Manual publishing for MR testing

## Publishing Logic

| Trigger | Behavior |
|---------|----------|
| **Master branch** | ✅ Automatic publish |
| **Git tags** | ✅ Automatic versioned publish |
| **Merge requests** | 🔘 Manual publish available |
| **Feature branches** | ❌ No publishing |

## Environment Variables

- `GRADLE_OPTS`: JVM options for Gradle
- `GRADLE_USER_HOME`: Gradle cache directory

## Cache Strategy

Global caching of:
- `.gradle/wrapper` - Gradle wrapper distribution
- `.gradle/caches` - Dependencies and build outputs

## Dependencies

The Kotlin DSL uses:
- `com.github.pcimcioch:gitlab-ci-kotlin-dsl:1.7.0`

## Example Usage

```kotlin
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