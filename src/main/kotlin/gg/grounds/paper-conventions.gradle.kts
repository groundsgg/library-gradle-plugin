package gg.grounds

plugins {
    id("gg.grounds.paper-base-conventions")
}

repositories { maven("https://repo.papermc.io/repository/maven-public/") }

dependencies { compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT") }

val pluginVersion: Any = project.version

tasks.withType<ProcessResources> {
    inputs.property("version", pluginVersion)
    filesMatching(listOf("**/plugin.yml")) { expand(mapOf("VERSION" to pluginVersion)) }
}
