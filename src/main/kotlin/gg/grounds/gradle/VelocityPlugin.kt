package gg.grounds.gradle

import com.github.gmazzo.buildconfig.BuildConfigExtension
import gg.grounds.gradle.common.addPaperRepository
import gg.grounds.gradle.common.configureShadowPackaging
import gg.grounds.gradle.common.getGroundsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class VelocityPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            val extension = getGroundsExtension()

            pluginManager.apply("com.github.gmazzo.buildconfig")

            addPaperRepository()
            configureShadowPackaging()

            afterEvaluate {
                if (extension.addDependencies.get()) {
                    dependencies.add(
                        "compileOnly",
                        "com.velocitypowered:velocity-api:3.4.0-SNAPSHOT",
                    )
                    dependencies.add("kapt", "com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
                }
            }

            extensions.configure(BuildConfigExtension::class.java) { buildConfig ->
                buildConfig.className("BuildInfo")
                buildConfig.packageName("gg.grounds")
                buildConfig.useKotlinOutput()
                buildConfig.buildConfigField("String", "VERSION", "\"${project.version}\"")
            }
        }
    }
}
