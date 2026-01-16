package gg.grounds

plugins { id("com.diffplug.spotless") }

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
