package gg.grounds

import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("com.diffplug.spotless")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
}

kotlin { jvmToolchain(25) }

tasks.test {
    useJUnitPlatform()
    testLogging { exceptionFormat = TestExceptionFormat.FULL }
}

repositories { mavenCentral() }

project.group = "gg.grounds"

val versionOverride = project.project.findProperty("versionOverride") as? String
project.version = versionOverride ?: "local-SNAPSHOT"

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
