plugins {
    `java-platform`
    `maven-publish`
}

group = "gg.grounds"

val versionOverride = project.findProperty("versionOverride") as? String

version = versionOverride ?: "local-SNAPSHOT"

javaPlatform { allowDependencies() }

dependencies {
    constraints {
        api("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
        api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.3.0")
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        api("com.google.protobuf:protobuf-java:4.34.1")
        api("io.grpc:grpc-api:1.81.0")
        api("io.grpc:grpc-core:1.81.0")
        api("io.grpc:grpc-context:1.81.0")
        api("io.grpc:grpc-stub:1.81.0")
        api("io.grpc:grpc-protobuf:1.81.0")
        api("io.grpc:grpc-netty-shaded:1.81.0")
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

    publications {
        create<MavenPublication>("groundsRuntimeBom") {
            from(components["javaPlatform"])
            artifactId = "grounds-runtime-bom"
        }
    }
}
