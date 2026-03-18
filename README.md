# Lognity

[![](https://gitlab.com/connect2x/lognity/badges/main/pipeline.svg)](https://gitlab.com/connect2x/lognity)

| Repository       | Version                                                                                                                                                                                                                                                                        |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Package Registry | [![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fgitlab.com%2Fapi%2Fv4%2Fprojects%2F72301746%2Fpackages%2Fmaven%2Fde%2Fconnect2x%2Flognity%2Flognity-api%2Fmaven-metadata.xml&strategy=latestProperty)](https://gitlab.com/connect2x/lognity/-/packages) |
| Maven Central    | [![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.maven.apache.org%2Fmaven2%2Fde%2Fconnect2x%2Flognity%2Flognity-core%2Fmaven-metadata.xml)](https://gitlab.com/connect2x/lognity/-/packages)                                                        |

A lightweight logging facade and implementation for Kotlin Multiplatform.  
It integrates with existing libraries and APIs like `java.logging`, **slf4j** and **Ktor**.

### How to use it

First, add the official Maven Central repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots")
        mavenCentral()
    }
}
```

Then add a dependency on the library in your root buildscript:

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("de.connect2x.lognity:lognity-api:<version>")
                implementation("de.connect2x.lognity:lognity-core:<version>")
            }
        }
    }
}
```

## Quickstart

> **Note:** for more detailed information about the features of this library,  
> please check out the [single-file wiki](WIKI.md)!

In order to get started with Lognity, you need to pick a backend implementation.

```kotlin
// Use the default backend provided by the lognity-core module
Backend.set(DefaultBackend)
```

> **Note:** if no backend is selected explicitly, a `NoopBackend` will be provided.

Optionally, you may also pick a default configuration for all newly created Loggers.

### Creating a Logger

You should always define your loggers as the interface type `Logger` from the `lognity-api` module.  
New instances of this type may be obtained using the `Logger` pseudo-constructor.

```kotlin
// Creates a new logger with the default name
val myLogger: Logger = Logger()

// Creates a new logger with the specified name
val myOtherLogger: Logger = Logger("Other")
```

### Examples

Examples on how to use the Lognity API can be found in the `lognity-example` module.