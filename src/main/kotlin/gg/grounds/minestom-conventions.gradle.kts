package gg.grounds

import org.gradle.api.attributes.java.TargetJvmVersion

plugins { id("gg.grounds.kotlin-conventions") }

dependencies { implementation("net.minestom:minestom:2026.01.08-1.21.11") }

configurations.configureEach {
    if (isCanBeResolved) {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
    }
}
