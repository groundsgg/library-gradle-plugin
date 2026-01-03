# library-gradle-plugin

Gradle plugin bundle for Grounds projects.

## Plugins

- `gg.grounds.root`: shared repo configuration (Kotlin, publishing, Spotless, test logging, ...).
- `gg.grounds.paper`: Paper server plugin defaults (Paper repo, shadow packaging, Paper API dep, ...).
- `gg.grounds.velocity`: Velocity proxy plugin defaults (Paper repo, shadow packaging, Velocity API deps, BuildConfig, ...).

## Usage

## Recommended setup (GitHub Packages)

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

Kotlin DSL (`build.gradle.kts`):

```kotlin
plugins {
    id("gg.grounds.root") version "0.1.0"
}
```

For Paper subprojects:

```kotlin
plugins {
    id("gg.grounds.paper") version "0.1.0"
}
```

For Velocity subprojects:

```kotlin
plugins {
    id("gg.grounds.velocity") version "0.1.0"
}
```

## Extension

Both `gg.grounds.paper` and `gg.grounds.velocity` use the same `grounds` extension.
Set `addDependencies` to control whether the plugin adds the Paper/Velocity API dependencies.

Kotlin DSL (`build.gradle.kts`):

```kotlin
grounds {
    addDependencies.set(false)
}
```
