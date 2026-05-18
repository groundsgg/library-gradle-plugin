package gg.grounds.runtime

data class RuntimeLibrary(val group: String, val name: String) {
    val notation: String = "$group:$name"
}

object RuntimeLibraries {
    val shared =
        listOf(
            RuntimeLibrary("org.jetbrains.kotlin", "kotlin-stdlib"),
            RuntimeLibrary("org.jetbrains.kotlin", "kotlin-stdlib-jdk8"),
            RuntimeLibrary("org.jetbrains.kotlinx", "kotlinx-coroutines-core"),
            RuntimeLibrary("com.google.protobuf", "protobuf-java"),
            RuntimeLibrary("io.grpc", "grpc-api"),
            RuntimeLibrary("io.grpc", "grpc-core"),
            RuntimeLibrary("io.grpc", "grpc-context"),
            RuntimeLibrary("io.grpc", "grpc-stub"),
            RuntimeLibrary("io.grpc", "grpc-protobuf"),
            RuntimeLibrary("io.grpc", "grpc-netty-shaded"),
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
