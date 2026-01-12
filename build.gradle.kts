plugins {
    kotlin("jvm") version "2.3.0"
    id("com.diffplug.spotless") version "8.1.0"
    `java-gradle-plugin`
    `maven-publish`
}

if (project == rootProject) {
    tasks.withType<PublishToMavenLocal>().configureEach { enabled = false }
    tasks.withType<PublishToMavenRepository>().configureEach { enabled = false }
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")

    group = "gg.grounds"

    val versionOverride = project.findProperty("versionOverride") as? String

    version = versionOverride ?: "local-SNAPSHOT"

    repositories { mavenCentral() }

    dependencies {
        implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.3.0")
        implementation("org.jetbrains.kotlin.kapt:org.jetbrains.kotlin.kapt.gradle.plugin:2.3.0")
        implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.3.1")
        implementation("com.diffplug.spotless:spotless-plugin-gradle:8.1.0")
    }

    kotlin { jvmToolchain(25) }

    spotless {
        kotlin {
            ktfmt().googleStyle().configure {
                it.setBlockIndent(4)
                it.setContinuationIndent(4)
            }
            targetExclude("**/build/**")
        }
        kotlinGradle {
            ktfmt().googleStyle().configure {
                it.setBlockIndent(4)
                it.setContinuationIndent(4)
            }
            targetExclude("**/build/**")
        }
    }
}

subprojects {
    apply(plugin = "java-gradle-plugin")
    apply(plugin = "maven-publish")

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/groundsgg/${rootProject.name}")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}
