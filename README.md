# library-gradle-plugin

Gradle convention plugins for Grounds projects.

## Usage

### Making the plugins available to your project

Add the GitHub Packages repo to `pluginManagement` so Gradle can resolve the
`gg.grounds.*` plugins via the `plugins {}` block.

Kotlin DSL (`settings.gradle.kts`):

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/groundsgg/*")
            credentials {
                username = providers.gradleProperty("github.user").get()
                password = providers.gradleProperty("github.token").get()
            }
        }
        gradlePluginPortal()
    }
}
```

### Add the appropriate convention plugin

In your `build.gradle.kts`, apply the convention plugin that fits your project type. Only set the version on `gg.grounds.base-conventions`, other `gg.grounds.*-conventions` plugins must omit the version.

For all projects (base configuration):

```kotlin
plugins {
    id("gg.grounds.base-conventions") version "VERSION"
}
```

For just Kotlin projects (usually the `common` module):

```kotlin
plugins {
    id("gg.grounds.kotlin-conventions")
}
```

For Paper projects:

```kotlin
plugins {
    id("gg.grounds.paper-conventions")
}
```

For Velocity projects:

```kotlin
plugins {
    id("gg.grounds.velocity-conventions")
}
```

For Minestom projects:

```kotlin
plugins {
    id("gg.grounds.minestom-conventions")
}
```

For gRPC projects:

```kotlin
plugins {
    id("gg.grounds.grpc-conventions")
}
```

### Overriding paper, velocity or minestom version

The consumer Gradle project can request a higher paper or velocity version, but **not** a lower one.

- The current Paper version: 1.21.11-R0.1-SNAPSHOT
- The current Velocity version: 3.4.0-SNAPSHOT
- The current Minestom version: 2026.01.08-1.21.11

```kotlin
// Example for Override

dependencies {
    // This will override the current version with 1.21.12-R0.1-SNAPSHOT
    compileOnly("io.papermc.paper:paper-api:1.21.12-R0.1-SNAPSHOT")
}
```

## Migration Guide from 0.1.x to 0.2.x

### Plugin Migration

The `gg.grounds.root` plugin does not exist anymore. Remove it.
All necessary configuration is configured by the respective convention plugins.

| Old plugin name       | Migration steps                               | 
|-----------------------|-----------------------------------------------|
| `gg.grounds.root`     | Use `gg.grounds.base-conventions` instead.    |
| `gg.grounds.paper`    | Use `gg.grounds.paper-conventions` instead    |
| `gg.grounds.velocity` | Use `gg.grounds.velocity-conventions` instead |

### Extension Migration 

The `GroundsExtension` is not required and was removed therefore, since Gradle allows version overriding (at least for higher versions) out of the box.
If a newer paper or velocity version is required, it can just be added to the dependencies.

```kotlin
// Example for Override

dependencies {
    // This will override the current version with 1.21.12-R0.1-SNAPSHOT
    compileOnly("io.papermc.paper:paper-api:1.21.12-R0.1-SNAPSHOT") 
}
```

## Plugins Hierarchy

```mermaid
graph TB
  kotlin["kotlin-conventions"] --> base["base-conventions"]
  minestom["minestom-conventions"] --> kotlin
  paperbase["paper-base-conventions"] --> kotlin
  grpc["grpc-conventions"] --> kotlin
  paper["paper-conventions"] --> paperbase
  velocity["velocity-conventions"] --> paperbase
```

A convention plugin defines shared configuration.
The diagram above shows the relation between the configuration plugins.
The diagram only shows the names without their prefixes.

- `base-conventions`: Setups spotless, common repositories (maven central) and version override 
- `kotlin-conventions`: Setups all Kotlin related configurations
- `minestom-conventions`: Minestom server plugin defaults (Minestom repo)
- `grpc-conventions`: Common configuration for gRPC
- `paper-base-conventions`: Common configuration for velocity and paper plugins
- `paper-conventions`: Paper server plugin defaults (Paper repo, shadow packaging, Paper API dep, ...)
- `velocity-conventions`: Velocity proxy plugin defaults (Paper repo, shadow packaging, Velocity API deps, BuildConfig, ...)

## Troubleshooting

### IntelliJ underlines some things red

![](docs_images/error_screenshot.png)

This happens because IntelliJ cannot find the type safe script accessors.
This is only an IntelliJ error, building the plugins does not result in an error.
The error can be fixed by first running `gradle clean`, 
then `gradle generatePrecompiledScriptPluginAccessors` 
and lastly tell IntelliJ to sync the Gradle Project again.

If it still does not work, you can use `configure<EXTENSION CLASS>`.
For that you need to find the name of the Extension Class.
You can either try to guess it (for example, the `spotless` extension probable has spotless in its name).
Alternatively, you can add the plugin to a regular Gradle project, and make CTRL + Left Click to see the Extensions class name.

To make sure it is just an IntelliJ error, try to build (`gradle build`) the project.

```kotlin
// Example to configure spotless (which is equivalent to just write `spotless`) 

configure<SpotlessExtension> {

}
```

## License

Licensed under the Apache License, Version 2.0.
