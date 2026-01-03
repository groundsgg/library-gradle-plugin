dependencies { implementation(project(":common")) }

gradlePlugin {
    plugins {
        create("groundsPaper") {
            id = "gg.grounds.paper"
            implementationClass = "gg.grounds.gradle.PaperPlugin"
        }
    }
}
