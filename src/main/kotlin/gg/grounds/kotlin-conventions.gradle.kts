package gg.grounds

import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `maven-publish`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    id("gg.grounds.base-conventions")
}

kotlin { jvmToolchain(25) }

tasks.test {
    useJUnitPlatform()
    testLogging { exceptionFormat = TestExceptionFormat.FULL }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/groundsgg/${project.rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("java") {
            from(project.components.getByName("java"))
            artifactId =
                if (project == project.rootProject) {
                    project.rootProject.name
                } else {
                    "${project.rootProject.name}-${project.name}"
                }
        }
    }
}
