import org.gradle.kotlin.dsl.embeddedKotlinVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.diffplug.spotless") version "8.2.0"
    `java-gradle-plugin`
    `maven-publish`
    `kotlin-dsl`
}

group = "gg.grounds"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(
        "org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$embeddedKotlinVersion"
    )
    implementation(
        "org.jetbrains.kotlin.kapt:org.jetbrains.kotlin.kapt.gradle.plugin:$embeddedKotlinVersion"
    )
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.1.0")
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.3.1")
    implementation(
        "com.github.gmazzo.buildconfig:com.github.gmazzo.buildconfig.gradle.plugin:6.0.7"
    )
    implementation("com.google.protobuf:com.google.protobuf.gradle.plugin:0.9.6")
}

val versionOverride = project.findProperty("versionOverride") as? String

version = versionOverride ?: "local-SNAPSHOT"

repositories { mavenCentral() }

kotlin { jvmToolchain(25) }

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

tasks.withType<JavaCompile>().configureEach { options.release.set(24) }

tasks.withType<KotlinCompile>().configureEach { compilerOptions.jvmTarget.set(JvmTarget.JVM_24) }

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
