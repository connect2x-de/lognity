# Lognity Logstash Config

Adds the `logstash` appender type to `lognity-config`, following the extension model used by
`lognity-core-config`. Supports JVM, Android, and Native, like `lognity-logstash`; web targets
are not supported.

```kotlin
implementation("de.connect2x.lognity:lognity-logstash-config:<version>")
```

Register extensions **before the first configuration is loaded**:

```kotlin
import de.connect2x.lognity.config.CoreConfigExtension
import de.connect2x.lognity.config.LogstashConfigExtension
import de.connect2x.lognity.config.SerializableConfig

SerializableConfig uses CoreConfigExtension // Optional: core appenders and filter conditions
SerializableConfig uses LogstashConfigExtension
val config = SerializableConfig.load(source).asConfig()
```

`CoreConfigExtension` requires the separate `lognity-core-config` dependency. The Logstash
extension does not select a backend; configure your backend as usual.

```json
{
  "version": 1,
  "appenders": [
    {
      "type": "logstash",
      "name": "remote",
      "destinations": ["logs.example.com:4560", "[::1]:4560"],
      "connection_strategy": {
        "type": "prefer_primary",
        "min_connection_time_before_primary": "PT10S",
        "secondary_connection_ttl": "PT60S"
      },
      "keep_alive_duration": "PT30S",
      "encoder": {
        "custom_fields": {"service": "my-app", "metadata": {"environment": "production"}},
        "field_names": {"message": "text", "@version": "[ignore]"}
      }
    }
  ]
}
```

Only `destinations` is required beyond `type`. A destination without a port uses `4560`;
IPv6 literals must be bracketed. The standard `pattern`, `formatter`, `filter`, and `name`
options are supported, defaulting to `{{message}}`, `default`, an accepting filter, and no name.
Messages are formatted by Lognity before JSON encoding; the encoder adds its own newline.

## Options

All durations use ISO-8601 strings, such as `PT0.1S`, `PT5S`, or `-PT0.001S`.
Validation is delegated to the runtime when converting with `asConfig()`.

| Option | Default | Meaning |
|--------|---------|---------|
| `ring_buffer_size` | `8192` | Queue capacity; a positive power of two. |
| `append_timeout` | `PT0S` | Zero drops on a full queue; negative waits indefinitely; positive waits up to the duration. |
| `reconnection_delay` | `PT30S` | Positive, finite retry delay. |
| `connection_timeout` | `PT5S` | Nonnegative, finite connect timeout; zero disables it. |
| `write_timeout` | `PT0S` | Nonnegative, finite write timeout; zero disables it. |
| `write_buffer_size` | `8192` | Nonnegative batching buffer size; zero disables batching. |
| `keep_alive_duration` | `null` | Positive, finite heartbeat interval; null disables heartbeats. |
| `keep_alive_message` | `"\n"` | Heartbeat text, encoded as UTF-8. |
| `keep_alive_bytes` | `null` | Optional array of signed bytes (`-128..127`), overriding heartbeat text. |
| `shutdown_grace_period` | `PT60S` | Nonnegative, finite time allowed to drain on shutdown. |
| `flush_timeout` | `PT60S` | Positive, finite flush timeout. |
| `tls` | `false` | Enable TLS using platform trust and destination hostname verification (JVM/Android only). |

`connection_strategy` accepts `prefer_primary` (default), `round_robin`, or `random` as its
`type`. `prefer_primary` accepts `min_connection_time_before_primary` (default `PT10S`,
nonnegative and finite) and `secondary_connection_ttl` (default null). The other strategies
accept `connection_ttl` (default null). Non-null TTLs must be positive and finite.

`encoder` accepts `custom_fields` (arbitrary JSON values), `field_names` (renaming map, with
`[ignore]` to omit a field), and `line_separator` (default `"\n"`).

Options use the same `RefOrValue` providers and contextual templates as core configuration.
Destination entries can interpolate providers, for example `"{logstash.host}:{logstash.port}"`.
Providers for duration values must return Kotlin `Duration` values, not strings. Providers for
whole destination lists return `List<RefOrValue<String>>`; custom-field maps contain
`RefOrValue<JsonElement>` values. Nested JSON objects in custom fields are kept as literal JSON.

TLS on Native is rejected during configuration conversion, never downgraded to plaintext.
Custom TLS builders, socket options/factories, listeners, dispatchers, clocks, strategies,
and encoder callbacks remain available through the programmatic `lognity-logstash` API.

Created appenders start automatically. On shutdown, call `stop()` or `stopSuspend()` on each
runtime `de.connect2x.lognity.logstash.LogstashAppender` to drain accepted messages and close
connections. See the [runtime documentation](../lognity-logstash/README.md) for delivery semantics.