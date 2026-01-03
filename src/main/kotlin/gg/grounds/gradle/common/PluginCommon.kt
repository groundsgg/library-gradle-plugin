package gg.grounds.gradle.common

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

internal fun Project.addPaperRepository() {
    repositories.maven { it.url = uri("https://repo.papermc.io/repository/maven-public/") }
}

internal fun Project.configureShadowPackaging() {
    pluginManager.apply("com.gradleup.shadow")

    tasks.named("build") { task -> task.dependsOn("shadowJar") }

    tasks.named("jar", Jar::class.java) { task -> task.enabled = false }

    tasks.named("shadowJar", ShadowJar::class.java) { task ->
        task.archiveBaseName.set(rootProject.name)
        task.archiveClassifier.set("")
        task.archiveVersion.set("")
    }
}
