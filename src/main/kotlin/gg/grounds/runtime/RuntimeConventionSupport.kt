package gg.grounds.runtime

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.zip.ZipFile
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.kotlin.dsl.create
import org.gradle.language.jvm.tasks.ProcessResources

fun Project.configurePaperRuntimeConvention(flavor: RuntimeConventionFlavor) {
    repositories.maven(
        action { repository ->
            repository.url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    )
    dependencies.add("compileOnly", "io.papermc.paper:paper-api:26.1.2.build.63-stable")

    val pluginVersion = project.version
    tasks
        .withType(ProcessResources::class.java)
        .configureEach(
            action { processResources ->
                processResources.inputs.property("version", pluginVersion)
                processResources.filesMatching(
                    listOf("**/plugin.yml"),
                    action { fileDetails -> fileDetails.expand(mapOf("VERSION" to pluginVersion)) },
                )
            }
        )

    configureRuntimeFlavor(flavor)

    if (flavor == RuntimeConventionFlavor.RuntimeConsumer) {
        registerPaperMetadataValidation()
    }
}

fun Project.configureVelocityRuntimeConvention(flavor: RuntimeConventionFlavor) {
    repositories.maven(
        action { repository ->
            repository.url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    )
    dependencies.add("compileOnly", "com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    dependencies.add("kapt", "com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")

    extensions.configure(
        BuildConfigExtension::class.java,
        action { buildConfig ->
            buildConfig.className("BuildInfo")
            buildConfig.packageName("gg.grounds")
            buildConfig.useKotlinOutput()
            buildConfig.buildConfigField("String", "VERSION", "\"${project.version}\"")
        },
    )

    configureRuntimeFlavor(flavor)

    if (flavor == RuntimeConventionFlavor.RuntimeConsumer) {
        registerVelocityMetadataValidation()
    }
}

private fun Project.configureRuntimeFlavor(flavor: RuntimeConventionFlavor) {
    val extension = extensions.create<GroundsRuntimeExtension>("groundsRuntime")
    extension.runtimeVersion.convention(
        providers.gradleProperty("groundsRuntime.version").orElse("0.1.1")
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

    dependencyHandler.addProvider(
        configurationName,
        extension.runtimeVersion.map { runtimeVersion ->
            dependencyHandler.platform("gg.grounds:grounds-runtime-bom:$runtimeVersion")
        },
    )
    RuntimeLibraries.shared.forEach { runtimeLibrary ->
        dependencyHandler.add(configurationName, dependencyHandler.create(runtimeLibrary.notation))
    }
}

private fun Project.configureRuntimeRepositories() {
    repositories.mavenLocal()
    repositories.maven(
        action { repository ->
            repository.name = "GroundsGitHubPackages"
            repository.url = uri("https://maven.pkg.github.com/groundsgg/*")
            repository.credentials(
                PasswordCredentials::class.java,
                action { credentials ->
                    credentials.username =
                        providers
                            .gradleProperty("github.user")
                            .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                            .orNull
                    credentials.password =
                        providers
                            .gradleProperty("github.token")
                            .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                            .orNull
                },
            )
        }
    )
}

private fun Project.configureShadowRelocations(relocations: Map<String, String>) {
    tasks
        .named("shadowJar", ShadowJar::class.java)
        .configure(
            action { shadowJar ->
                relocations.forEach { (fromPackage, toPackage) ->
                    shadowJar.relocate(fromPackage, toPackage)
                }
            }
        )
}

private fun Project.excludeSharedRuntimeDependenciesFromShadowJar() {
    tasks
        .named("shadowJar", ShadowJar::class.java)
        .configure(
            action { shadowJar ->
                shadowJar.dependencies(
                    action { dependencyFilter ->
                        RuntimeLibraries.sharedDependencyPatterns.forEach { pattern ->
                            dependencyFilter.exclude(dependencyFilter.dependency(pattern))
                        }
                    }
                )
            }
        )
}

private fun Project.registerPaperMetadataValidation() {
    val validateRuntimeConsumerMetadata =
        tasks.register(
            "validateRuntimeConsumerMetadata",
            action { task ->
                task.group = "verification"
                task.description = "Validates Paper runtime-consumer plugin metadata."

                task.doLast(
                    action {
                        val pluginYml = project.file("src/main/resources/plugin.yml")
                        if (
                            !pluginYml.isFile ||
                                !pluginYml.readText().contains("plugin-grounds-runtime")
                        ) {
                            throw GradleException(
                                "Runtime consumer metadata is missing required Paper dependency (dependency=plugin-grounds-runtime, file=src/main/resources/plugin.yml)"
                            )
                        }
                    }
                )
            },
        )

    tasks
        .named("check")
        .configure(action { task -> task.dependsOn(validateRuntimeConsumerMetadata) })
}

private fun Project.registerVelocityMetadataValidation() {
    val validateRuntimeConsumerMetadata =
        tasks.register(
            "validateRuntimeConsumerMetadata",
            action { task ->
                task.group = "verification"
                task.description = "Validates Velocity runtime-consumer plugin metadata."

                task.doLast(
                    action {
                        val sourceRoot = project.file("src/main/kotlin")
                        val hasDependency =
                            sourceRoot
                                .walkTopDown()
                                .filter { it.isFile && it.extension == "kt" }
                                .any {
                                    it.readText()
                                        .contains("Dependency(id = \"plugin-grounds-runtime\")")
                                }

                        if (!hasDependency) {
                            throw GradleException(
                                "Runtime consumer metadata is missing required Velocity dependency (dependency=plugin-grounds-runtime, class=<pluginClass>)"
                            )
                        }
                    }
                )
            },
        )

    tasks
        .named("check")
        .configure(action { task -> task.dependsOn(validateRuntimeConsumerMetadata) })
}

private fun Project.registerBundledSharedRuntimeValidation(extension: GroundsRuntimeExtension) {
    val validateNoBundledSharedRuntime =
        tasks.register(
            "validateNoBundledSharedRuntime",
            action { task ->
                task.group = "verification"
                task.description =
                    "Validates runtime-consumer artifacts do not bundle shared runtime classes."
                task.dependsOn(tasks.named("shadowJar"))

                task.doLast(
                    action {
                        if (extension.allowBundledSharedRuntime.orNull == true) {
                            logger.warn(
                                "Skipped shared runtime bundle validation (project={}, allowBundledSharedRuntime=true)",
                                project.path,
                            )
                            return@action
                        }

                        val artifact =
                            tasks
                                .named("shadowJar", ShadowJar::class.java)
                                .get()
                                .archiveFile
                                .get()
                                .asFile
                        ZipFile(artifact).use { zipFile ->
                            val bundledPrefix =
                                zipFile
                                    .entries()
                                    .asSequence()
                                    .map { it.name }
                                    .firstOrNull { entryName ->
                                        RuntimeLibraries.forbiddenConsumerJarPrefixes.any {
                                            forbiddenPrefix ->
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
                )
            },
        )

    tasks
        .named("check")
        .configure(action { task -> task.dependsOn(validateNoBundledSharedRuntime) })
}

private fun <T : Any> action(block: (T) -> Unit): Action<T> = BlockAction(block)

private class BlockAction<T : Any>(private val block: (T) -> Unit) : Action<T> {
    override fun execute(value: T) {
        block(value)
    }
}
