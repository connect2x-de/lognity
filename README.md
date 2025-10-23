# Lognity

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
                implementation("net.folivo.lognity:lognity-api:<version>")
                implementation("net.folivo.lognity:lognity-core:<version>")
            }
        }
    }
}
```

### Examples

Examples on how to use the Lognity API can be found in the `lognity-example` module.