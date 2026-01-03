package gg.grounds.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class RootPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            if (this != rootProject) {
                return
            }

            apply {
                it.plugin("maven-publish")
                it.plugin("org.jetbrains.kotlin.jvm")
                it.plugin("org.jetbrains.kotlin.kapt")
                it.plugin("com.diffplug.spotless")
            }

            allprojects.forEach { project ->
                project.pluginManager.apply("com.diffplug.spotless")

                project.repositories.apply { mavenCentral() }

                project.extensions.configure(SpotlessExtension::class.java) { spotless ->
                    spotless.kotlin { kt ->
                        kt.ktfmt().googleStyle().configure {
                            it.setBlockIndent(4)
                            it.setContinuationIndent(4)
                        }
                        kt.targetExclude("**/build/**")
                    }
                    spotless.kotlinGradle { kg ->
                        kg.ktfmt().googleStyle().configure {
                            it.setBlockIndent(4)
                            it.setContinuationIndent(4)
                        }
                        kg.targetExclude("**/build/**")
                    }
                }
            }

            // If there are no subprojects, configure the root project, otherwise configure all
            // subprojects so this works for single- and multi-module builds.
            val kotlinTargets = if (subprojects.isEmpty()) listOf(this) else subprojects
            kotlinTargets.forEach { subproject -> configureKotlinProject(subproject) }
        }
    }

    private fun configureKotlinProject(project: Project) {
        project.group = "gg.grounds"

        val versionOverride = project.project.findProperty("versionOverride") as? String
        project.version = versionOverride ?: "local-SNAPSHOT"

        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("org.jetbrains.kotlin.kapt")
        project.pluginManager.apply("maven-publish")

        project.extensions.configure(KotlinJvmProjectExtension::class.java) { it.jvmToolchain(25) }

        project.tasks.withType(Test::class.java).configureEach {
            it.useJUnitPlatform()
            it.testLogging { logging ->
                // Show assertion diffs in test output
                logging.exceptionFormat = TestExceptionFormat.FULL
            }
        }

        project.extensions.configure(PublishingExtension::class.java) { publishing ->
            publishing.repositories.maven { repo ->
                repo.name = "GitHubPackages"
                repo.url =
                    project.uri(
                        "https://maven.pkg.github.com/groundsgg/${project.rootProject.name}"
                    )
                repo.credentials { credentials ->
                    credentials.username = System.getenv("GITHUB_ACTOR")
                    credentials.password = System.getenv("GITHUB_TOKEN")
                }
            }

            publishing.publications.create("java", MavenPublication::class.java) { publication ->
                publication.from(project.components.getByName("java"))
                publication.artifactId =
                    if (project == project.rootProject) {
                        project.rootProject.name
                    } else {
                        "${project.rootProject.name}-${project.name}"
                    }
            }
        }
    }
}
