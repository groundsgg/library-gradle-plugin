import groovy.json.JsonSlurper
import org.gradle.kotlin.dsl.embeddedKotlinVersion

plugins {
    id("com.diffplug.spotless") version "8.5.0"
    `java-gradle-plugin`
    `maven-publish`
    `kotlin-dsl`
}

group = "gg.grounds"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(
        "org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$embeddedKotlinVersion"
    )
    implementation(
        "org.jetbrains.kotlin.kapt:org.jetbrains.kotlin.kapt.gradle.plugin:$embeddedKotlinVersion"
    )
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.5.0")
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.4.1")
    implementation(
        "com.github.gmazzo.buildconfig:com.github.gmazzo.buildconfig.gradle.plugin:6.0.9"
    )
    implementation("com.google.protobuf:com.google.protobuf.gradle.plugin:0.10.0")
}

val versionOverride = project.findProperty("versionOverride") as? String

version = versionOverride ?: "local-SNAPSHOT"

repositories { mavenCentral() }

val runtimeCatalogFile =
    layout.projectDirectory.file("runtime-catalog/grounds-runtime-libraries.json")
val generatedRuntimeLibrariesDir = layout.buildDirectory.dir("generated/runtime-catalog/kotlin")
val generatedRuntimeLibrariesFile =
    generatedRuntimeLibrariesDir.map { it.file("gg/grounds/runtime/RuntimeLibraries.kt") }

data class CatalogRuntimeLibrary(val group: String, val name: String, val version: String)

fun String.kotlinStringLiteral(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

fun Iterable<String>.toKotlinList(): String =
    joinToString(prefix = "listOf(\n", postfix = "\n        )", separator = ",\n") {
        "            ${it.kotlinStringLiteral()}"
    }

fun Map<String, String>.toKotlinMap(): String =
    entries.joinToString(prefix = "mapOf(\n", postfix = "\n        )", separator = ",\n") {
        "            ${it.key.kotlinStringLiteral()} to ${it.value.kotlinStringLiteral()}"
    }

fun runtimeCatalog(): Map<String, Any?> =
    (JsonSlurper().parse(runtimeCatalogFile.asFile) as? Map<*, *>)?.mapKeys { it.key as String }
        ?: error("Runtime catalog must be a JSON object")

fun Map<String, Any?>.objectList(name: String): List<Map<String, Any?>> =
    (getValue(name) as List<*>).mapIndexed { index, value ->
        (value as? Map<*, *>)?.mapKeys { it.key as String }
            ?: error("Runtime catalog field $name[$index] must be an object")
    }

fun Map<String, Any?>.stringMap(name: String): Map<String, String> =
    (getValue(name) as Map<*, *>)
        .map { (key, value) -> (key as String) to (value as String) }
        .toMap()

fun Map<String, Any?>.stringList(name: String): List<String> =
    (getValue(name) as List<*>).mapIndexed { index, value ->
        value as? String ?: error("Runtime catalog field $name[$index] must be a string")
    }

fun Map<String, Any?>.runtimeLibraries(): List<CatalogRuntimeLibrary> =
    objectList("libraries").mapIndexed { index, runtimeLibrary ->
        CatalogRuntimeLibrary(
            group =
                runtimeLibrary["group"] as? String
                    ?: error("Runtime catalog field libraries[$index].group must be a string"),
            name =
                runtimeLibrary["name"] as? String
                    ?: error("Runtime catalog field libraries[$index].name must be a string"),
            version =
                runtimeLibrary["version"] as? String
                    ?: error("Runtime catalog field libraries[$index].version must be a string"),
        )
    }

val generateRuntimeLibraries by
    tasks.registering {
        inputs.file(runtimeCatalogFile)
        outputs.file(generatedRuntimeLibrariesFile)

        doLast {
            val catalog = runtimeCatalog()
            val libraries = catalog.runtimeLibraries()
            val runtimeRelocations = catalog.stringMap("runtimeRelocations")
            val standaloneRelocations = catalog.stringMap("standaloneRelocations")
            val forbiddenConsumerJarPrefixes = catalog.stringList("forbiddenConsumerJarPrefixes")
            val sharedDependencyPatterns = catalog.stringList("sharedDependencyPatterns")
            val generatedFile = generatedRuntimeLibrariesFile.get().asFile

            generatedFile.parentFile.mkdirs()
            generatedFile.writeText(
                """
                |package gg.grounds.runtime
                |
                |data class RuntimeLibrary(val group: String, val name: String, val version: String) {
                |    val notation: String = "${'$'}group:${'$'}name:${'$'}version"
                |}
                |
                |object RuntimeLibraries {
                |    val shared =
                |        listOf(
                |${libraries.joinToString(separator = ",\n") { runtimeLibrary ->
                    "            RuntimeLibrary(${runtimeLibrary.group.kotlinStringLiteral()}, ${runtimeLibrary.name.kotlinStringLiteral()}, ${runtimeLibrary.version.kotlinStringLiteral()})"
                }}
                |        )
                |
                |    val runtimeRelocations =
                |        ${runtimeRelocations.toKotlinMap()}
                |
                |    val standaloneRelocations = ${standaloneRelocations.toKotlinMap()}
                |
                |    val forbiddenConsumerJarPrefixes =
                |        ${forbiddenConsumerJarPrefixes.toKotlinList()}
                |
                |    val sharedDependencyPatterns =
                |        ${sharedDependencyPatterns.toKotlinList()}
                |}
                |"""
                    .trimMargin()
            )
        }
    }

kotlin {
    jvmToolchain(25)
    sourceSets.named("main") { kotlin.srcDir(generatedRuntimeLibrariesDir) }
}

tasks.named("compileKotlin") { dependsOn(generateRuntimeLibraries) }

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

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/groundsgg/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
