# Logstash TCP appender

> **This feature is experimental!**  
> If you experience any issues or need 1:1 compatibility, use Logback Logstash Encoder via SLF4J
> as illustrated on the [external-backend-example](https://gitlab.com/connect2x/lognity/-/tree/external-backend-example/lognity-external-backend?ref_type=heads) branch.

`LogstashAppender` implements Lognity's `Appender` in `commonMain` using Ktor TCP sockets.
It sends to one destination at a time, like `net.logstash.logback.appender.LogstashTcpSocketAppender`.
It does **not** use Socket.IO or WebSockets.

## Programmatic configuration

```kotlin
import de.connect2x.lognity.api.logger.Logger
import de.connect2x.lognity.logstash.LogstashAppender
import de.connect2x.lognity.logstash.LogstashConfig
import de.connect2x.lognity.logstash.LogstashDestination
import de.connect2x.lognity.logstash.LogstashJsonEncoder
import de.connect2x.lognity.logstash.PreferPrimaryStrategy
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Duration.Companion.seconds

val stash = LogstashAppender(
    config = LogstashConfig(
        destinations = listOf(
            LogstashDestination.parse("logs.example.com:4560"),
            LogstashDestination.parse("[::1]:4560")
        ),
        connectionStrategy = PreferPrimaryStrategy(secondaryConnectionTTL = 60.seconds),
        keepAliveDuration = 30.seconds,
        writeTimeout = 5.seconds
    ),
    encoder = LogstashJsonEncoder(customFields = mapOf("service" to JsonPrimitive("orders"))),
    name = "logstash"
)

Logger.defaultConfig = { appender(stash) }
```

Keep a reference to the appender and call `stash.stop()` (or `stopSuspend()`) before application exit.
`close()` is equivalent to `stop()`. Construction starts the writer; use `autoStart = false` and
`start()` for explicit startup. Stop is idempotent; create a new instance to restart logging.
`flush()` / `flushSuspend()` wait for a FIFO barrier behind previously accepted events.
They throw on timeout rather than wait forever for an unavailable destination.

The receiver should use Logstash's TCP input with `codec => json_lines`:

```text
input {
    tcp {
        port => 4560
        codec => json_lines
    }
}
```

## Transport and lifecycle options

| Option | Default | Behavior |
|---|---|---|
| `destinations` | required | Host/port pairs; parser accepts hostnames, IPv4, and bracketed IPv6; omitted port is 4560 |
| `connectionStrategy` | `PreferPrimaryStrategy()` | Prefer primary, round robin, random, or a custom strategy; optional connection TTLs |
| `ringBufferSize` | 8192 | Positive power-of-two bounded coroutine queue capacity |
| `appendTimeout` | zero | Drop on full queue; positive waits up to that duration; negative waits indefinitely |
| `reconnectionDelay` | 30 seconds | Minimum interval between attempts to each individual destination |
| `connectionTimeout` | 5 seconds | Includes TCP connect and TLS handshake; zero disables |
| `writeTimeout` | zero | Bounds writes/flush and graceful socket shutdown; zero disables |
| `writeBufferSize` | 8192 bytes | Batch size; zero writes each event; partial batches flush when the queue empties |
| `keepAliveDuration` | null | Optional idle heartbeat interval |
| `keepAliveMessage` | `"\n"` | Literal UTF-8 heartbeat; empty disables; use `"\r\n"` for Windows framing |
| `keepAliveBytes` | null | Optional already-encoded heartbeat, overriding the message |
| `shutdownGracePeriod` | 60 seconds | Maximum graceful drain time before cancellation; zero stops immediately |
| `flushTimeout` | 60 seconds | Bounds an explicit flush barrier |
| `tls` | null | Optional Ktor TLS configuration; JVM/Android only |
| `socketOptions` | Ktor defaults | Low-level TCP configuration such as TCP_NODELAY and send-buffer size |
| `socketFactory` | Ktor | Replaceable transport, including custom TLS implementations |
| `listeners` | empty | Connection, accepted/sent/dropped/failed event, and lifecycle notifications |
| `dispatcher` | Single-parallelism view of `Dispatchers.IO` | Writer scheduling independent of blocked Default-dispatcher producers |
| `timeSource` | monotonic | Time source for reconnect, TTL, and heartbeat scheduling |

There is one coroutine writer, not a Disruptor ring buffer or a Java thread factory. Java-specific
wait strategies, thread naming, daemon flags, and Logback XML/status-system integration do not apply.
The queue bounds waiting commands; the writer may additionally hold the current event and one byte-bounded
batch. Default append does not wait for networking or encoding. Positive/negative append timeouts can
block synchronous callers; use `appendSuspend` for coroutine-friendly backpressure.
If supplying a dispatcher, ensure it can make progress independently of synchronous logging callers.

`LogstashListener` callbacks must be thread-safe and nonblocking. Do not call blocking append, flush,
or stop on the same appender from callbacks/encoders. Listener exceptions are isolated. Monitor
`droppedEventCount`, `eventDropped`, `eventFailed`, and connection failures to detect loss/outages.
No listener is installed automatically, avoiding recursive use of the application's logger.

## TLS on JVM and Android

Enable TLS using `tls = {}`. The default trusts the system trust store and verifies the destination
hostname. In JVM/Android source code the Ktor builder also exposes `trustManager`, `cipherSuites`,
and `addKeyStore(...)` for private certificate authorities and client certificates:

```kotlin
import io.ktor.network.tls.addKeyStore

val tlsConfig = LogstashConfig(
    destinations = listOf(LogstashDestination("logs.example.com", 4560)),
    tls = {
        trustManager = applicationTrustManager
        addKeyStore(clientKeyStore, clientKeyPassword)
    }
)
```

TLS protocols/ciphers are those supported by Ktor's TLS implementation (not Logback's JSSE SSLContext).
Never use a trust-all manager in production. Android applications must declare
`android.permission.INTERNET` in their manifest.

Native TCP remains supported on the module's existing targets. **Native TLS is intentionally not
implemented**, as Ktor does not support native TLS sessions; enabling it with the default transport
throws at configuration time. There is no plaintext fallback. Browser/JS targets are not enabled.

## Encoding

The default encoder sends UTF-8 JSON Lines with `@timestamp`, `@version`, `message`, `logger_name`,
`level`, `level_value`, and optional marker `tags`. JSON escaping preserves multiline messages as
one wire record. `LogstashJsonEncoder` supports custom fields, per-event fields, renamed fields,
`"[ignore]"` field suppression, and a configurable line separator. Implement `LogstashEncoder` for
other formats/charsets, custom framing, or per-connection headers and footers. Encoding runs on the writer.

Lognity passes already-formatted messages, not Logback `ILoggingEvent`s. Thread name, MDC, throwable
objects, argument arrays, and caller data are not supplied by this appender interface. They are not
fabricated; additional fields/encoders can use the available immutable logger context. Existing Lognity
formatters can be supplied through `pattern` and `formatter` for message formatting.

TCP has no application-level delivery acknowledgement. A successful flush is not proof that Logstash
indexed an event. Retrying a failed batch can duplicate data after partial delivery; process failure or
the shutdown deadline can lose pending data. There is no persistent queue or exactly-once guarantee.

## Verification

Run tests through Gradle:

```sh
./gradlew :lognity-logstash:jvmTest :lognity-logstash:testDebugUnitTest :lognity-logstash:linuxX64Test
```

Tests include virtual-time backpressure/retry/lifecycle cases, concurrent producers, real TCP,
large-payload shutdown, and JVM/Android TLS certificate verification and mutual authentication.