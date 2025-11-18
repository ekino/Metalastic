plugins {
    `java-platform`
    alias(libs.plugins.gradle.maven.publish.plugin)
}

group = "com.ekino.oss"
// Version inherited from root project (set from git tags)

dependencies {
    constraints {
        api(project(":modules:core"))
        api(project(":modules:processor"))
        api(project(":modules:gradle-plugin"))
        api(project(":modules:elasticsearch-dsl-5.3"))
        api(project(":modules:elasticsearch-dsl"))
    }
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "metalastic-bom",
        version = project.version.toString()
    )

    publishToMavenCentral(automaticRelease = true)

    // Only sign if credentials are available (skip for publishToMavenLocal)
    if (project.hasProperty("signingInMemoryKey")) {
        signAllPublications()
    }
    
    pom {
        name.set("Metalastic BOM")
        description.set("Bill of Materials for Metalastic - ensures version alignment across all artifacts")
        url.set("https://github.com/ekino/Metalastic")
        
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        
        developers {
            developer {
                id.set("Benoit.Havret")
                name.set("Benoît Havret")
                email.set("benoit.havret@ekino.com")
                organization.set("ekino")
                organizationUrl.set("https://github.com/ekino")
            }
        }
        
        scm {
            connection.set("scm:git:git://github.com/ekino/Metalastic.git")
            developerConnection.set("scm:git:ssh://github.com/ekino/Metalastic.git")
            url.set("https://github.com/ekino/Metalastic")
        }
    }
}
