# Lognity

[![](https://gitlab.com/connect2x/lognity/badges/main/pipeline.svg)](https://gitlab.com/connect2x/lognity)

| Repository       | Version                                                                                                                                                                                                                                                                        |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Package Registry | [![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fgitlab.com%2Fapi%2Fv4%2Fprojects%2F72301746%2Fpackages%2Fmaven%2Fde%2Fconnect2x%2Flognity%2Flognity-api%2Fmaven-metadata.xml&strategy=latestProperty)](https://gitlab.com/connect2x/lognity/-/packages) |
| Maven Central    | [![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fde%2Fconnect2x%2Flognity%2Flognity-api%2Fmaven-metadata.xml)](https://gitlab.com/connect2x/lognity/-/packages)                                    |

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

In order to get started with Lognity, you need to pick a backend implementation.

```kotlin
// Use the default backend provided by the lognity-core module
Backend.set(DefaultBackend)
```

> Note: if no backend is selected explicitly, a `NoopBackend` will be provided.

Optionally, you may also pick a default configuration for all newly created Loggers.

---

### Programmatic configuration

Lognity can be configured using a type-safe DSL if no external configuration file is needed.

```kotlin
Backend.configSpec = {
    consoleAppender(...) // Use the config DSL
}
```

---

### File based configuration

You can also use the `lognity-config` module to configure logging using an external config file;  
First you register all desired config extensions using the `SerializableConfig` DSL:

```kotlin
fun main() {
    SerializableConfig uses CoreConfigExtension
}
```

> **Note**: Config extensions are provided by the respective `*-config` modules corresponding  
> to the underlying implementation modules. For example, in order to use config extensions for  
> the `lognity-core` module, we need to add the `lognity-core-config` module.

Then you can load a config, either using the convenience wrapper `withDefaultConfig`,  
or one of the other provided `load*Config` functions.

```kotlin
// Make main suspend and use withDefaultConfig if you need to support web
suspend fun main() {
    Backend.withDefaultConfig("my_config.json") {
        // Do stuff with logging in here to ensure config is loaded
    }
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

#### Config providers

Config providers offer a convenient way to pass dynamic data from your code to the JSON configuration.  
This is done through a special reference syntax `{...}` which gets resolved to dynamic values registered  
with the `SerializableConfig` class.

The following illustrates a simple example on how to use config providers:

```kotlin
fun main() {
    Backend.set(DefaultBackend)
    SerializableConfig uses CoreConfigExtension
    SerializableConfig uses ConfigExtension {
        registerProvider("MY_DYNAMIC_PATH") { /* ... */ }
    }
    // Load config, setup loggers etc..
}
```

Which can then be used in the JSON config:

```json
{
    // ...
    "appenders": [
        {
            "type": "rolling_file",
            "base_path": "{MY_DYNAMIC_PATH}/logfile.log",
            // ...
        }
    ]
}
```

> **Tip**: in order to get a hint which properties may use config providers,  
> it is strongly recommended to use the Lognity config JSON schema.

#### Template providers

If contextual resolution is required for a given provider, **template providers** may be used
instead of regular static ones.  
Template providers allow access to the current `SerializableConfig` instance and dynamic arguments.
A template provider may be invoked using the `{prefix:name}` notation in the JSON config:

```json
{
    "conditions": [
        {
            "name": "my_condition",
            // ...
        }
    ],
    "appenders": [
        {
            // ...
            "filter": {
                "conditions": [
                    "{conditions:my_condition}"
                ]
            }
        }
    ]
}
```

Custom template providers can be registered using `ConfigExtensionRegistrar.registerTemplateProvider`.

#### Parametrized template providers

Template providers may also accept a variable number of arguments in the form of a limited set of  
constant expressions using the parametrized template notation `{prefix:name(params...)}`:

```json
{
    "conditions": [
        {
            "name": "my_condition",
            // ...
        }
    ],
    "appenders": [
        {
            // ...
            "filter": {
                "conditions": [
                    "{conditions:my_condition(<Level.TRACE>)}"
                ]
            }
        }
    ]
}
```

However as of right now, while the config system supports passing arguments and handling them programmatically,
this is unused in the core config system.  
A further extension allowing scoped references is planned, but doesn't have a fixed ETA.

---

### Creating a Logger

You should always define your loggers as the interface type `Logger` from the `lognity-api` module.  
New instances of this type may be obtained using the `Logger` pseudo-constructor.

```kotlin
// Creates a new logger with the default name
val myLogger: Logger = Logger()

// Creates a new logger with the specified name
val myOtherLogger: Logger = Logger("Other")
```

---

### Examples

Examples on how to use the Lognity API can be found in the `lognity-example` module.