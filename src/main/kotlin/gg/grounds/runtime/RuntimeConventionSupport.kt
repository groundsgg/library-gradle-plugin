package gg.grounds.runtime

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.zip.ZipFile
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

fun Project.configurePaperRuntimeConvention(flavor: RuntimeConventionFlavor) {
    repositories.maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    dependencies { add("compileOnly", "io.papermc.paper:paper-api:26.1.2.build.63-stable") }

    val pluginVersion = project.version
    tasks.withType<ProcessResources> {
        inputs.property("version", pluginVersion)
        filesMatching(listOf("**/plugin.yml")) { expand(mapOf("VERSION" to pluginVersion)) }
    }

    configureRuntimeFlavor(flavor)

    if (flavor == RuntimeConventionFlavor.RuntimeConsumer) {
        registerPaperMetadataValidation()
    }
}

fun Project.configureVelocityRuntimeConvention(flavor: RuntimeConventionFlavor) {
    repositories.maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    dependencies {
        add("compileOnly", "com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
        add("kapt", "com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    }

    configure<BuildConfigExtension> {
        className("BuildInfo")
        packageName("gg.grounds")
        useKotlinOutput()
        buildConfigField("String", "VERSION", "\"${project.version}\"")
    }

    configureRuntimeFlavor(flavor)

    if (flavor == RuntimeConventionFlavor.RuntimeConsumer) {
        registerVelocityMetadataValidation()
    }
}

private fun Project.configureRuntimeFlavor(flavor: RuntimeConventionFlavor) {
    val extension = extensions.create<GroundsRuntimeExtension>("groundsRuntime")
    extension.runtimeVersion.convention(
        providers.gradleProperty("groundsRuntime.version").orElse("0.1.0")
    )

    when (flavor) {
        RuntimeConventionFlavor.Standalone ->
            configureShadowRelocations(RuntimeLibraries.standaloneRelocations)
        RuntimeConventionFlavor.RuntimeConsumer -> {
            addSharedRuntimeDependencies("compileOnly", extension)
            configureShadowRelocations(RuntimeLibraries.runtimeRelocations)
            excludeSharedRuntimeDependenciesFromShadowJar()
            registerBundledSharedRuntimeValidation(extension)
        }
        RuntimeConventionFlavor.RuntimeProvider -> {
            addSharedRuntimeDependencies("implementation", extension)
            configureShadowRelocations(RuntimeLibraries.runtimeRelocations)
        }
    }
}

private fun Project.addSharedRuntimeDependencies(
    configurationName: String,
    extension: GroundsRuntimeExtension,
) {
    configureRuntimeRepositories()
    val dependencyHandler = dependencies

    configurations.named(configurationName) {
        withDependencies {
            add(
                dependencyHandler.platform(
                    "gg.grounds:grounds-runtime-bom:${extension.runtimeVersion.get()}"
                )
            )
            RuntimeLibraries.shared.forEach { runtimeLibrary ->
                add(dependencyHandler.create(runtimeLibrary.notation))
            }
        }
    }
}

private fun Project.configureRuntimeRepositories() {
    repositories.mavenLocal()
    repositories.maven {
        name = "GroundsGitHubPackages"
        url = uri("https://maven.pkg.github.com/groundsgg/*")
        credentials {
            username =
                providers
                    .gradleProperty("github.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                    .orNull
            password =
                providers
                    .gradleProperty("github.token")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                    .orNull
        }
    }
}

private fun Project.configureShadowRelocations(relocations: Map<String, String>) {
    tasks.named<ShadowJar>("shadowJar") {
        relocations.forEach { (fromPackage, toPackage) -> relocate(fromPackage, toPackage) }
    }
}

private fun Project.excludeSharedRuntimeDependenciesFromShadowJar() {
    tasks.named<ShadowJar>("shadowJar") {
        dependencies {
            RuntimeLibraries.sharedDependencyPatterns.forEach { pattern ->
                exclude(dependency(pattern))
            }
        }
    }
}

private fun Project.registerPaperMetadataValidation() {
    val validateRuntimeConsumerMetadata =
        tasks.register("validateRuntimeConsumerMetadata") {
            group = "verification"
            description = "Validates Paper runtime-consumer plugin metadata."

            doLast {
                val pluginYml = project.file("src/main/resources/plugin.yml")
                if (!pluginYml.isFile || !pluginYml.readText().contains("plugin-grounds-runtime")) {
                    throw GradleException(
                        "Runtime consumer metadata is missing required Paper dependency (dependency=plugin-grounds-runtime, file=src/main/resources/plugin.yml)"
                    )
                }
            }
        }

    tasks.named("check") { dependsOn(validateRuntimeConsumerMetadata) }
}

private fun Project.registerVelocityMetadataValidation() {
    val validateRuntimeConsumerMetadata =
        tasks.register("validateRuntimeConsumerMetadata") {
            group = "verification"
            description = "Validates Velocity runtime-consumer plugin metadata."

            doLast {
                val sourceRoot = project.file("src/main/kotlin")
                val hasDependency =
                    sourceRoot
                        .walkTopDown()
                        .filter { it.isFile && it.extension == "kt" }
                        .any {
                            it.readText().contains("Dependency(id = \"plugin-grounds-runtime\")")
                        }

                if (!hasDependency) {
                    throw GradleException(
                        "Runtime consumer metadata is missing required Velocity dependency (dependency=plugin-grounds-runtime, class=<pluginClass>)"
                    )
                }
            }
        }

    tasks.named("check") { dependsOn(validateRuntimeConsumerMetadata) }
}

private fun Project.registerBundledSharedRuntimeValidation(extension: GroundsRuntimeExtension) {
    val validateNoBundledSharedRuntime =
        tasks.register("validateNoBundledSharedRuntime") {
            group = "verification"
            description =
                "Validates runtime-consumer artifacts do not bundle shared runtime classes."
            dependsOn(tasks.named("shadowJar"))

            doLast {
                if (extension.allowBundledSharedRuntime.get()) {
                    logger.warn(
                        "Skipped shared runtime bundle validation (project={}, allowBundledSharedRuntime=true)",
                        project.path,
                    )
                    return@doLast
                }

                val artifact = tasks.named<ShadowJar>("shadowJar").get().archiveFile.get().asFile
                ZipFile(artifact).use { zipFile ->
                    val bundledPrefix =
                        zipFile
                            .entries()
                            .asSequence()
                            .map { it.name }
                            .firstOrNull { entryName ->
                                RuntimeLibraries.forbiddenConsumerJarPrefixes.any { forbiddenPrefix
                                    ->
                                    entryName.startsWith(forbiddenPrefix)
                                }
                            }

                    if (bundledPrefix != null) {
                        val packagePrefix =
                            RuntimeLibraries.forbiddenConsumerJarPrefixes.first {
                                bundledPrefix.startsWith(it)
                            }
                        throw GradleException(
                            "Runtime consumer artifact bundles shared runtime classes (artifact=${artifact.path}, package=$packagePrefix)"
                        )
                    }
                }
            }
        }

    tasks.named("check") { dependsOn(validateNoBundledSharedRuntime) }
}
