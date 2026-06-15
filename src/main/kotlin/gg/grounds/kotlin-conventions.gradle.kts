package gg.grounds

import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar

plugins {
    `maven-publish`
    id("gg.grounds.base-conventions")
}

val sourcesJarEnabled =
    providers
        .gradleProperty("grounds.kotlin.sourcesJar.enabled")
        .map(String::toBoolean)
        .getOrElse(true)
val dokkaEnabled =
    providers.gradleProperty("grounds.kotlin.dokka.enabled").map(String::toBoolean).getOrElse(true)

if (sourcesJarEnabled) {
    extensions.configure<JavaPluginExtension> { withSourcesJar() }
}

val dokkaJavadocJar =
    if (dokkaEnabled) {
        plugins.apply("org.jetbrains.dokka")
        plugins.apply("org.jetbrains.dokka-javadoc")

        val dokkaJavadoc = tasks.named("dokkaGeneratePublicationJavadoc")
        tasks.register<Jar>("dokkaJavadocJar") {
            group = "documentation"
            description = "Assembles a jar archive containing Dokka Javadoc documentation."
            archiveClassifier.set("javadoc")
            dependsOn(dokkaJavadoc)
            from(dokkaJavadoc.map { it.outputs.files })
        }
    } else {
        null
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
            if (dokkaJavadocJar != null) {
                artifact(dokkaJavadocJar)
            }
            artifactId =
                if (project == project.rootProject) {
                    project.rootProject.name
                } else {
                    "${project.rootProject.name}-${project.name}"
                }
        }
    }
}
