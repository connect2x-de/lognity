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

### Quickstart

In order to get started with Lognity, you need to pick a backend implementation.

```kotlin
// Use the default backend provided by the lognity-core module
Backend.set(DefaultBackend)
```

> Note: if no backend is selected explicitly, a `NoopBackend` will be provided.

Optionally, you may also pick a default configuration for all newly created Loggers.

#### Programmatic configuration

```kotlin
Backend.configSpec = {
    consoleAppender(...) // Use the config DSL
}
```

#### File based configuration

You can also use the `lognity-config` module to configure logging using an external config file:

```kotlin
Backend.withDefaultConfig("my_config.json") {
    // Do stuff with logging in here to ensure config is loaded
}
```

`my_config.json`:

> **Tip**: the `scheme` directory in the root of this repository contains a premade JSON schema  
> for the Lognity config format.

```json
{
    "version": 1,
    "level": "TRACE",
    "enabled": true,
    "appenders": [
        {
            "type": "system_console",
            "name": "my_appender",
            "pattern": "{{levelColor}}>>  {{levelSymbol}}\t{{hh}}:{{mm}}:{{ss}}.{{SSS}} ({{name}} @ {{threadId}}) {{message}}{{r}}",
            "formatter": "default",
            "filter": {
                "conditions": [
                    {
                        "type": "always"
                    }
                ]
            }
        }
    ]
}
```

#### Creating a Logger

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