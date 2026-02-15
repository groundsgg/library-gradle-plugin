package gg.grounds

plugins {
    id("gg.grounds.kotlin-conventions")
    id("com.google.protobuf")
}

val grpcVersion = "1.78.0"
val protobufVersion = "4.29.0"

dependencies {
    api("com.google.protobuf:protobuf-java:${protobufVersion}")
    implementation("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:$protobufVersion" }

    plugins { create("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion" } }

    generateProtoTasks { all().forEach { task -> task.plugins { create("grpc") } } }
}
