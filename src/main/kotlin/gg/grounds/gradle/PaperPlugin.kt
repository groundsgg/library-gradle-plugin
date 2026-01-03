package gg.grounds.gradle

import gg.grounds.gradle.common.addPaperRepository
import gg.grounds.gradle.common.configureShadowPackaging
import gg.grounds.gradle.common.getGroundsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.jvm.tasks.ProcessResources

class PaperPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            val extension = getGroundsExtension()

            addPaperRepository()
            configureShadowPackaging()

            afterEvaluate {
                if (extension.addDependencies.get()) {
                    dependencies.add(
                        "compileOnly",
                        "io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT",
                    )
                }
            }

            val pluginVersion = project.version.toString()
            tasks.named("processResources", ProcessResources::class.java) { task ->
                task.inputs.property("version", project.version)
                task.filesMatching(listOf("**/plugin.yml")) { files ->
                    files.expand(mapOf("VERSION" to pluginVersion))
                }
            }
        }
    }
}
