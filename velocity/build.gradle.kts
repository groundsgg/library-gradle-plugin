dependencies {
    implementation(project(":common"))
    implementation(
        "com.github.gmazzo.buildconfig:com.github.gmazzo.buildconfig.gradle.plugin:6.0.7"
    )
}

gradlePlugin {
    plugins {
        create("groundsVelocity") {
            id = "gg.grounds.velocity"
            implementationClass = "gg.grounds.gradle.VelocityPlugin"
        }
    }
}
