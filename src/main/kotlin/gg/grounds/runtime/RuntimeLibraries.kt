package gg.grounds.runtime

data class RuntimeLibrary(val group: String, val name: String, val version: String) {
    val notation: String = "$group:$name:$version"
}

object RuntimeLibraries {
    val shared =
        listOf(
            RuntimeLibrary("org.jetbrains.kotlin", "kotlin-stdlib", "2.3.0"),
            RuntimeLibrary("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "2.3.0"),
            RuntimeLibrary("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.10.2"),
            RuntimeLibrary("com.google.protobuf", "protobuf-java", "4.34.1"),
            RuntimeLibrary("io.grpc", "grpc-api", "1.81.0"),
            RuntimeLibrary("io.grpc", "grpc-core", "1.81.0"),
            RuntimeLibrary("io.grpc", "grpc-context", "1.81.0"),
            RuntimeLibrary("io.grpc", "grpc-stub", "1.81.0"),
            RuntimeLibrary("io.grpc", "grpc-protobuf", "1.81.0"),
            RuntimeLibrary("io.grpc", "grpc-netty-shaded", "1.81.0"),
        )

    val runtimeRelocations =
        mapOf(
            "io.grpc" to "gg.grounds.runtime.libs.grpc",
            "com.google.protobuf" to "gg.grounds.runtime.libs.protobuf",
        )

    val standaloneRelocations = mapOf("com.google.protobuf" to "gg.grounds.shaded.protobuf")

    val forbiddenConsumerJarPrefixes =
        listOf(
            "kotlin/",
            "kotlinx/coroutines/",
            "com/google/protobuf/",
            "io/grpc/",
            "META-INF/maven/com.google.protobuf/",
            "META-INF/maven/io.grpc/",
        )

    val sharedDependencyPatterns =
        listOf(
            "org.jetbrains.kotlin:.*:.*",
            "org.jetbrains.kotlinx:.*:.*",
            "com.google.protobuf:.*:.*",
            "io.grpc:.*:.*",
        )
}
