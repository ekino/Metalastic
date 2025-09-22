# Metalastic Publishing & Consumption Guide

## Publishing to GitLab Maven Registry

This project is configured to automatically publish to GitLab's Maven Package Registry.

### Automatic Publishing

The CI/CD pipeline automatically publishes:
- **Snapshots** on feature branch pushes
- **Releases** on master branch pushes and tags

### Manual Publishing

To publish manually (requires GitLab authentication):
```bash
./gradlew publish
```

## Consuming the Library

### Adding GitLab Maven Repository

Add the GitLab Package Registry to your project:

#### For Project Members (Recommended)

If you're a member of the Metalastic project, no credentials are needed:

##### Gradle (Kotlin DSL)
```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://gitlab.ekino.com/api/v4/projects/{PROJECT_ID}/packages/maven")
        // No credentials needed for project members
    }
}
```

##### Gradle (Groovy DSL)
```groovy
repositories {
    mavenCentral()
    maven {
        url 'https://gitlab.ekino.com/api/v4/projects/{PROJECT_ID}/packages/maven'
        // No credentials needed for project members
    }
}
```

##### Maven
```xml
<repositories>
    <repository>
        <id>gitlab-maven</id>
        <url>https://gitlab.ekino.com/api/v4/projects/{PROJECT_ID}/packages/maven</url>
    </repository>
</repositories>
```

#### For External Users (with Personal Access Token)

If you're not a project member, you'll need credentials:

##### Gradle (Kotlin DSL)
```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://gitlab.ekino.com/api/v4/projects/{PROJECT_ID}/packages/maven")
        credentials(HttpHeaderCredentials::class) {
            name = "Private-Token"
            value = "{YOUR_PERSONAL_ACCESS_TOKEN}"
        }
        authentication {
            create("header", HttpHeaderAuthentication::class)
        }
    }
}
```

##### Gradle (Groovy DSL)
```groovy
repositories {
    mavenCentral()
    maven {
        url 'https://gitlab.ekino.com/api/v4/projects/{PROJECT_ID}/packages/maven'
        credentials(HttpHeaderCredentials) {
            name = 'Private-Token'
            value = '{YOUR_PERSONAL_ACCESS_TOKEN}'
        }
        authentication {
            header(HttpHeaderAuthentication)
        }
    }
}
```

##### Maven
```xml
<repositories>
    <repository>
        <id>gitlab-maven</id>
        <url>https://gitlab.ekino.com/api/v4/projects/{PROJECT_ID}/packages/maven</url>
    </repository>
</repositories>

<servers>
    <server>
        <id>gitlab-maven</id>
        <configuration>
            <httpHeaders>
                <property>
                    <name>Private-Token</name>
                    <value>{YOUR_PERSONAL_ACCESS_TOKEN}</value>
                </property>
            </httpHeaders>
        </configuration>
    </server>
</servers>
```

### Adding Dependencies

#### For Kotlin Projects
```kotlin
dependencies {
    implementation("com.metalastic:core:1.0-SNAPSHOT")
    ksp("com.metalastic:processor:1.0-SNAPSHOT")
}
```

#### For Java Projects  
```kotlin
dependencies {
    implementation("com.metalastic:core:1.0-SNAPSHOT")
    annotationProcessor("com.metalastic:processor:1.0-SNAPSHOT")
}
```

### Setting Up Authentication

#### Personal Access Token
1. Go to GitLab → User Settings → Access Tokens
2. Create token with `read_api` and `read_repository` scopes
3. Replace `{YOUR_PERSONAL_ACCESS_TOKEN}` with your token

#### Deploy Token (Project-level)
1. Go to Project → Settings → Repository → Deploy Tokens
2. Create token with `read_repository` and `read_package_registry` scopes
3. Use username/token as credentials

### Environment Variables
For CI/CD or to avoid hardcoding tokens:
```bash
export GITLAB_TOKEN=your_token_here
```

Then in build.gradle.kts:
```kotlin
value = System.getenv("GITLAB_TOKEN")
```

### Project ID
Replace `{PROJECT_ID}` with the actual GitLab project ID. You can find it:
- On the project's main page under the project name
- In the project URL
- Via GitLab API: `GET /projects/:path_with_namespace`

## Version Management

- Development versions: `1.0-SNAPSHOT`
- Release versions: `1.0.0`, `1.1.0`, etc.
- Tag releases for stable versions: `git tag v1.0.0`

## Troubleshooting

### Authentication Issues
- Verify token has correct scopes
- Check token expiration
- Ensure project visibility settings allow access

### Dependency Resolution Issues
- Clear Gradle cache: `./gradlew --refresh-dependencies`
- Check version compatibility
- Verify repository URL and credentials