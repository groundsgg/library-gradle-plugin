package gg.grounds

plugins {
    `maven-publish`
    id("gg.grounds.base-conventions")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/groundsgg/${project.rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("java") {
            from(project.components.getByName("java"))
            artifactId =
                if (project == project.rootProject) {
                    project.rootProject.name
                } else {
                    "${project.rootProject.name}-${project.name}"
                }
        }
    }
}
