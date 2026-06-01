# Lognity Wiki

Welcome to the single-file wiki for the Lognity logging library.

- [0. Project structure]()
- [1. Using Lognity in your project](#1-using-lognity-in-your-project)
- [2. Choosing a backend](#2-choosing-a-backend)
- [3. Configuration](#3-configuration)
    - [3.1. Programmatic configuration](#31-programmatic-configuration)
        - [3.1.1. Appenders](#311-appenders)
        - [3.1.2. Formatters](#312-formatters)
        - [3.1.3. Filters](#313-filters)
        - [3.1.4. Overrides](#314-overrides)
        - [3.1.5. Colors](#315-colors)
    - [3.2. File based configuration](#32-file-based-configuration)
        - [3.2.1. Appenders](#321-appenders)
        - [3.2.2. Filters](#322-filters)
        - [3.2.3. Overrides](#323-overrides)
        - [3.2.4. Providers](#324-providers)
        - [3.2.5. Colors](#325-colors)
    - [3.3. Environment based configuration](#33-environment-based-configuration)
        - [3.3.1. JVM](#331-jvm)
        - [3.3.2. Native](#332-native)
        - [3.3.3. Browser](#333-browser)
        - [3.3.4. NodeJS](#334-nodejs)

## 0. Project structure

Lognity is built from the ground up to be as modular as possible, to allow third
party implementations of entire logging backends using the standalone API.
This design philosophy extends far into the config system as well.  
In order to decide which of these modules you need for your own project,  
consider the following matrix:

| Module               | Description                                                                                                                                | Platforms   |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-------------|
| lognity-api          | The standalone Lognity logging API.<br>If you're writing a library/framework, you should only depend on this module.                       | KMP         |
| lognity-core         | The default standalone implementation of the Lognity logging API.<br>If your project is an application, you want to depend on this module. | KMP         |
| lognity-config       | The standalone Lognity config API.<br>If you're writing a Lognity extension with JSON config support, you want to depend on this module.   | KMP         |
| lognity-core-config  | Exposes features from `lognity-core` through the `lognity-config` config system.                                                           | KMP         |
| lognity-test         | Provides a special `TestBackend` and default configuration for logging in unit tests.                                                      | KMP         |
| lognity-java-logging | Provides integration between the Lognity logging API and the Java logging facade.                                                          | JVM/Android |
| lognity-ktor-client  | Provides integration between the Lognity logging API and the Ktor Client logging interface.                                                | KMP         |                                               
| lognity-ktor-server  | Provides integration between the Lognity logging API and the Ktor Server logging interface.                                                | KMP         |                                             
| lognity-slf4j        | Provides integration between the Lognity logging API and the SLF4j API.                                                                    | JVM/Android |

## 1. Using Lognity in your project

In order to use Lognity in your project, you first need to add the Maven Central  
repository to your buildscript or settings script.
It is recommended to use the `dependencyResolutionManagement` feature in Gradle as follows:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

The minimal setup requires the `lognity-api` module and an implementation to  
go with it. Please refer to the section [0. Project structure](#0-project-structure) to see which modules  
are available to use.

When you have picked which modules you want to use, just add them to your dependencies:

**Kotlin Multiplatform:**

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

**Kotlin JVM:**

```kotlin
dependencies {
    implementation("de.connect2x.lognity:lognity-api:<version>")
    implementation("de.connect2x.lognity:lognity-core:<version>")
}
```

## 2. Choosing a backend

Lognity allows choosing an arbitrary backend, the actual implementation of the logging system, at runtime.  
This means before creating any loggers in your application or library, you need to make the Lognity API aware of  
the backend that you want to use.

Using the default implementation provided by the `lognity-core` module, we can use the `Backend.set` function  
to set the backend as follows:

```kotlin
import de.connect2x.lognity.api.backend.Backend
import de.connect2x.lognity.backend.DefaultBackend

fun main() {
    Backend.set(DefaultBackend)
    // Start creating loggers after setting the backend
}
```

## 3. Configuration

The Lognity logging API provides two major means of configuration: programmatic and file based.  
The configuration is what defines the behavior of all the logger created through the Lognity API.

### 3.1. Programmatic configuration

The Lognity API provides a programmatic configuration interface out of the box.  
Immutable configurations are modeled by the `Config` type, while most of the time, you will encounter `ConfigSpec`,
which is a type-alias for a `ConfigBuilder.() -> Unit`.  
It allows passing a configuration directly as a trailing closure, as the global `Config` DSL function does.

```kotlin
import de.connect2x.lognity.api.config.Config

val myConfig: Config = Config {
    // Configuration using  DSL
}
```

> Note: inspect `de.connect2x.lognity.api.config.ConfigBuilder` to get a list of
> all available DSL functions.

#### 3.1.1. Appenders

Appenders effectively model IO-sinks which the `Logger` instance pipes the messages into after some basic  
pre-filtering was applied.  
An appender could be stdout, a file, a socket or any other type of data sink.

Consider the following implementation as an example:

```kotlin
import de.connect2x.lognity.api.appender.Appender

class MyAppender(
    override val pattern: String,
    override val formatter: Formatter = Formatter.default,
    override val filter: Filter = Filter.always,
    override val name: String? = null
) : Appender {
    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        println(message) // Log the message directly to the console
    }

    override fun flush() = Unit
}
```

You can add a custom appender implementation to your configuration using the `appender` DSL function:

```kotlin
import de.connect2x.lognity.api.config.Config

val myConfig: Config = Config {
    appender(MyAppender("{{message}}"))
}
```

Lognity also provides various builtin appenders for logging to the console and files.  
The following table describes all available builtin appender types that come with `lognity-core`:

| DSL function            | Description                                                                         |
|-------------------------|-------------------------------------------------------------------------------------|
| `consoleAppender`       | Logs to stdout using Kotlin's own `println` function. (No stderr support)           |
| `systemLogAppender`     | Logs to the underlying system log mechanism if present or to the console otherwise. | 
| `systemConsoleAppender` | Logs to stdout/stderr directly using the platform specific APIs.                    |
| `fileAppender`          | Logs to a single file.                                                              |
| `rollingFileAppender`   | Logs to one or more files in a round-robbin fashion based on file size.             |

#### 3.1.2. Formatters

Formatters are what `Appender`s use to pre-process the raw log message into a specified message pattern.
Usually, you want to use the default formatter exposed by `Formatter.default`, which provides highly optimized  
string processing out of the box.

For implementing your own formatter, consider the following example:

```kotlin
import de.connect2x.lognity.api.format.Formatter

// A simple formatter which ignores the pattern string 
// and always evaluates to the message content
val myFormatter: Formatter = object : Formatter {
    override operator fun invoke(
        logger: Logger,
        level: Level,
        content: Any,
        marker: Marker?,
        timestamp: Instant,
        s: String
    ): String = content.toString()
}
```

#### 3.1.3. Filters

Filters decide which messages reach which appenders.  
They can be used to create a separate log file for a service or detect markers for example.  
Usually, appenders will default to using `Filter.always`, which lets through every message.

Lognity also offers various other builtin filter types, including but not limited to:

| Name                    | Description                                                 |
|-------------------------|-------------------------------------------------------------|
| `Filter.levels`         | Lets through messages at the specified level(s).            |
| `Filter.levelsExcept`   | Lets through all messages except at the specified level(s). |
| `Filter.markers`        | Lets through all messages with the given marker(s).         |
| `Filter.containsString` | Lets through all messages that contain a given substring.   |

For implementing your own filter, consider the following example:

```kotlin
import de.connect2x.lognity.api.appender.Filter

// A simple filter which only lets through messages from
// a set of loggers based on their package name.
val myFilter: Filter = object : Filter {
    override operator fun invoke(
        logger: Logger,
        level: Level,
        message: String,
        marker: Marker?
    ): Boolean = logger.name.startsWith("com.example.")
}
```

#### 3.1.4. Overrides

Overrides are for providing dynamic overrides for per-logger properties globally,
like the log level and default enable state of the logger.  
They use conditions similar to the ones described in [3.1.3. Filters](#313-filters) to match against
the logger instance, the message, the message level and the marker.

Consider the following example to enable tracing for a given package:

```kotlin
import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.logger.Level

val myConfig: Config = Config {
    override {
        // A per-message predicate that decides if the override applies
        applyWhen { logger, _, _ -> logger.name.startsWith("com.example.") }
        // Any per-logger values to be overridden when the above predicate evaluates to true
        level = Level.TRACE
        enableState = true
    }
}
```

> Note: inspect `de.connect2x.lognity.api.config.Override` to get a list of
> all available overridable properties.

#### 3.1.5. Colors

Lognity offers various customization points and APIs for dealing with different
types of consoles, including the detection of console color schemes on supported targets.  
This allows Lognity to always pick the optimal default colors for your console.

However, this might not satisfy the requirements of every use case, so colors can be customized  
via the configuration using the `LevelColors` API:

```kotlin
import de.connect2x.lognity.api.config.Config
import de.connect2x.lognity.api.ansi.AnsiFg
import de.connect2x.lognity.api.ansi.AnsiBg
import de.connect2x.lognity.api.ansi.on

val myConfig: Config = Config {
    levelColors {
        debug(AnsiFg.hiCyan on AnsiBg.default)
    }
}
```

> Note: inspect `de.connect2x.lognity.api.config.LevelColorsBuilder` to get a list of
> all available DSL functions.

### 3.2. File based configuration

The `lognity-config` modules provides an
extensible, [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)-based JSON config system,
to allow logging configuration outside the code.  
Most of the concepts provided by the programmatic configuration API luckily translate very well into the JSON format.

The configuration is modeled by the `SerializableConfig` class from the `lognity-config` module.
and its basic required shape looks like this:

```json5
{
    "version": 1
    // Specifies the config version
}
```

Every other property in the configuration is optional.

#### 3.2.1. Appenders

Appenders from the `lognity-core` module are available in the JSON config with  
the `lognity-core-config` module present.  
The JSON configuration accepts the following appender types:

| Name             | Platforms                 |
|------------------|---------------------------|
| `console`        | KMP                       |
| `system_console` | KMP                       |
| `system_log`     | KMP                       |
| `logcat`         | Android, Android Native   |
| `winevent`       | Windows Native            |
| `os_log`         | macOS, iOS, tvOS, watchOS |

An appender can be specified as follows:

```json5
{
    "version": 1,
    "appenders": [
        {
            "type": "system_console",
            "name": "my_appender",
            "pattern": "{{message}}",
            // Any additional properties
        }
    ]
}
```

You may specify as many appenders as you like in the appenders array property.

#### 3.2.2. Filters

A filter in the JSON configuration system consists of a list of conditions,  
which are joined by conjunction, that is, a short-circuit AND `&&`.  
It can be added to any appender as follows:

```json5
{
    "version": 1,
    "appenders": [
        {
            // Your appender definition goes here
            "filter": {
                "conditions": [
                    // Define a condition which only evaluates to true for messages at a level >= WARN
                    {
                        "type": "level",
                        "condition": "ABOVE",
                        "value": "INFO"
                    }
                ]
            }
        }
    ]
}
```

Filter conditions also support disjunctive joining through the `or` compound condition:

```json5
{
    "version": 1,
    "appenders": [
        {
            // Your appender definition goes here
            "filter": {
                "conditions": [
                    {
                        "type": "or",
                        "conditions": [
                            // Define a condition which only evaluates to true for messages at a level >= WARN
                            {
                                "type": "level",
                                "condition": "ABOVE",
                                "value": "INFO"
                            },
                            // Define a condition which only evaluates to true for messages at a level <= DEBUG
                            {
                                "type": "level",
                                "condition": "BELOW",
                                "value": "INFO"
                            }
                        ]
                    }
                ]
            }
        }
    ]
}
```

The following table illustrates all conditions available from the `lognity-config` and `lognity-config-core` modules:

| Name             | Properties           | Subconditions                                                                                                                                      | Description                                                                              |
|------------------|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|
| `always`         | -                    | -                                                                                                                                                  | Always lets through messages.                                                            |
| `and`            | `conditions`         | -                                                                                                                                                  | Conjunctively joins a list of conditions.                                                |
| `or`             | `conditions`         | -                                                                                                                                                  | Disjunctively joins a list of conditions.                                                |
| `exactly_one`    | `conditions`         | -                                                                                                                                                  | True if exactly one of its subconditions is true.                                        |
| `level`          | `condition`, `value` | `EQUALS`, `NOT_EQUALS`, `ABOVE`, `BELOW`                                                                                                           | True if the message level comparison to the specified level succeeds.                    |
| `marker`         | `condition`, `value` | `KEY_EQUALS`, `KEY_NOT_EQUALS`, `KEY_CONTAINS`, `KEY_NOT_CONTAINS`, `KEY_STARTS_WITH`, `KEY_NOT_STARTS_WITH`, `KEY_ENDS_WITH`, `KEY_NOT_ENDS_WITH` | True if the given value matches against the specified part of the marker.                |
| `logger_name`    | `condition`, `value` | `EQUALS`, `NOT_EQUALS`, `CONTAINS`, `NOT_CONTAINS`, `STARTS_WITH`, `NOT_STARTS_WITH`, `ENDS_WITH`, `NOT_ENDS_WITH`                                 | True if the condition matches the given value against the name of the logger.            |
| `coroutine_name` | `condition`, `value` | `EQUALS`, `NOT_EQUALS`, `CONTAINS`, `NOT_CONTAINS`, `STARTS_WITH`, `NOT_STARTS_WITH`, `ENDS_WITH`, `NOT_ENDS_WITH`                                 | True if the condition matches the given value against the name of the current coroutine. |
| `message`        | `condition`, `value` | `EQUALS`, `NOT_EQUALS`, `CONTAINS`, `NOT_CONTAINS`, `STARTS_WITH`, `NOT_STARTS_WITH`, `ENDS_WITH`, `NOT_ENDS_WITH`                                 | True if the condition matches the given value agsinst the message content.               |

#### 3.2.3. Overrides

Override conditions work similarly to filter conditions, except that they do not support any form of joining (as of
right now).  
An override may be defined as follows:

```json5
{
    "version": 1,
    "overrides": [
        // Define a global override so all loggers whose name contain the package-prefix
        // "com.example." will output at TRACE level
        {
            "condition": {
                "type": "logger_name",
                "condition": "STARTS_WITH",
                "value": "com.example."
            },
            "level": "TRACE"
        }
    ]
}
```

You may specify as many overrides as you like.  
The following condition types are available for overrides:

| Name             | Properties           | Subconditions                                                                                                                                      | Description                                                                              |
|------------------|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|
| `level`          | `condition`, `value` | `EQUALS`, `NOT_EQUALS`, `ABOVE`, `BELOW`                                                                                                           | True if the message level comparison to the specified level succeeds.                    |
| `marker`         | `condition`, `value` | `KEY_EQUALS`, `KEY_NOT_EQUALS`, `KEY_CONTAINS`, `KEY_NOT_CONTAINS`, `KEY_STARTS_WITH`, `KEY_NOT_STARTS_WITH`, `KEY_ENDS_WITH`, `KEY_NOT_ENDS_WITH` | True if the given value matches against the specified part of the marker.                |
| `logger_name`    | `condition`, `value` | `EQUALS`, `NOT_EQUALS`, `CONTAINS`, `NOT_CONTAINS`, `STARTS_WITH`, `NOT_STARTS_WITH`, `ENDS_WITH`, `NOT_ENDS_WITH`                                 | True if the condition matches the given value against the name of the logger.            |
| `coroutine_name` | `condition`, `value` | `EQUALS`, `NOT_EQUALS`, `CONTAINS`, `NOT_CONTAINS`, `STARTS_WITH`, `NOT_STARTS_WITH`, `ENDS_WITH`, `NOT_ENDS_WITH`                                 | True if the condition matches the given value against the name of the current coroutine. |

#### 3.2.4. Providers

Providers allow passing data from the code into the JSON configuration system, so the code can pass things like  
directory paths into the configuration file, which may be different between platforms for example.

You can register a provider programmatically using the Lognity config DSL:

```kotlin
fun main(args: Array<String>) {
    SerializableConfig uses CoreConfigExtensions
    SerializableConfig uses ConfigExtension {
        registerProvider("MY_PROPERTY") { args.firstOrNull() ?: "" }
    }
}
```

This will expose the value provided in the trailing closure to the JSON configuration under the name `MY_PROPERTY`.  
The provider may be accessed using the single-brace syntax `{NAME}`:

```json5
{
    "version": 1,
    "appenders": [
        {
            "type": "file",
            "pattern": "{{message}}",
            // The value of MY_PROPERTY will be interpolated with the string it is embedded in
            "path": "{MY_PROPERTY}/latest.log",
            // More properties
        }
    ]
}
```

For more detailed information on providers, check the KDoc of the `ConfigExtensionRegistrar` class.

#### 3.2.5. Colors

Log colors as described by [3.1.5. Colors](#315-colors) can also be configured when using  
the JSON config system:

```json5
{
    "version": 1,
    "level_colors": {
        // Use pre-defined color constants that expand to ANSI codes..
        "DEBUG": "{Foreground.CYAN}{Background.DEFAULT}",
        // ..or go completely wild and use your own custom strings
        "INFO": "HELLO!",
        // ...
    }
}
```

> Note: to get a full list of all available pre-defined colors,
> check the `SerializableAnsiBg` and `SerializableAnsiFg` enums.

### 3.3. Environment based configuration

Being able to quickly change the logging configuration without touching any source code or config files is
handy for temporary changes during development and especially troubleshooting, since regular users don't want  
to deal with those things at all.

Configuration is applied with the following precedence:

#### `Environment Variables > JVM Properties > Instance Config > Global Config`

Where `Instance Config` means the per-Logger `Config` instance, and `Global Config` meaning the default  
configuration specification provided by the `Backend`.

#### 3.3.1. JVM

**VM arguments**

| Name                  | Values                                 |
|-----------------------|----------------------------------------|
| lognity.default.level | TRACE, DEBUG, INFO, WARN, ERROR, FATAL |

> Example: `java -Dlognity.default.level=TRACE -jar myjar.jar`

**Environment variables**

| Name                  | Values                                 |
|-----------------------|----------------------------------------|
| LOGNITY_DEFAULT_LEVEL | TRACE, DEBUG, INFO, WARN, ERROR, FATAL |

> Example: `LOGNITY_DEFAULT_LEVEL=TRACE java -jar myjar.jar`

#### 3.3.2. Native

**Environment variables**

| Name                  | Values                                 |
|-----------------------|----------------------------------------|
| LOGNITY_DEFAULT_LEVEL | TRACE, DEBUG, INFO, WARN, ERROR, FATAL |

> Example: `LOGNITY_DEFAULT_LEVEL=TRACE ./myapp.kexe`

#### 3.3.3. Browser

| Name     | Values                                 |
|----------|----------------------------------------|
| logLevel | TRACE, DEBUG, INFO, WARN, ERROR, FATAL |

> Example: `https://127.0.0.1:8080/?logLevel=TRACE`

#### 3.3.4. NodeJS

**Environment variables**

| Name                  | Values                                 |
|-----------------------|----------------------------------------|
| LOGNITY_DEFAULT_LEVEL | TRACE, DEBUG, INFO, WARN, ERROR, FATAL |

> Example: `LOGNITY_DEFAULT_LEVEL=TRACE npm start .`