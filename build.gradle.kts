plugins {
    kotlin("jvm") version "2.3.0"
    `java-gradle-plugin`
    `maven-publish`
    id("com.diffplug.spotless") version "8.1.0"
}

group = "gg.grounds"

val versionOverride = project.findProperty("versionOverride") as? String

version = versionOverride ?: "local-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.3.0")
    implementation("org.jetbrains.kotlin.kapt:org.jetbrains.kotlin.kapt.gradle.plugin:2.3.0")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.1.0")
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.3.0")
    implementation(
        "com.github.gmazzo.buildconfig:com.github.gmazzo.buildconfig.gradle.plugin:6.0.7"
    )
}

kotlin { jvmToolchain(25) }

gradlePlugin {
    plugins {
        create("groundsRoot") {
            id = "gg.grounds.root"
            implementationClass = "gg.grounds.gradle.RootPlugin"
        }
        create("groundsVelocity") {
            id = "gg.grounds.velocity"
            implementationClass = "gg.grounds.gradle.VelocityPlugin"
        }
        create("groundsPaper") {
            id = "gg.grounds.paper"
            implementationClass = "gg.grounds.gradle.PaperPlugin"
        }
    }
}

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
