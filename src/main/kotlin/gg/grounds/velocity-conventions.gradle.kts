package gg.grounds

import com.github.gmazzo.buildconfig.BuildConfigExtension

plugins {
    id("com.github.gmazzo.buildconfig")
    id("gg.grounds.paper-base-conventions")
}

repositories { maven("https://repo.papermc.io/repository/maven-public/") }

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

configure<BuildConfigExtension> {
    className("BuildInfo")
    packageName("gg.grounds")
    useKotlinOutput()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
}
