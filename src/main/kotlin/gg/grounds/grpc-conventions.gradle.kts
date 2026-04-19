package gg.grounds

plugins {
    id("gg.grounds.kotlin-conventions")
    id("com.google.protobuf")
}

val grpcVersion = "1.80.0"
val protobufVersion = "4.34.1"

dependencies {
    compileOnly("com.google.protobuf:protobuf-java:${protobufVersion}")
    implementation("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:$protobufVersion" }

    plugins { create("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion" } }

    generateProtoTasks { all().configureEach { plugins { create("grpc") } } }
}
