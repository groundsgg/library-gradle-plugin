gradlePlugin {
    plugins {
        create("groundsRoot") {
            id = "gg.grounds.root"
            implementationClass = "gg.grounds.gradle.RootPlugin"
        }
    }
}
