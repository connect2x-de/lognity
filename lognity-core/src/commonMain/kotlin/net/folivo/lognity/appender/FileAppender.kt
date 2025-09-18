/*
 * Copyright 2025 Trixnity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.folivo.lognity.appender

import co.touchlab.stately.collections.SharedHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import net.folivo.lognity.api.Level
import net.folivo.lognity.api.Logger
import net.folivo.lognity.api.Marker
import net.folivo.lognity.api.ansi.toAnsi
import net.folivo.lognity.api.appender.Appender
import net.folivo.lognity.api.appender.Filter
import net.folivo.lognity.api.format.Formatter
import net.folivo.lognity.backend.withBlockingLock
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch

@OptIn(ExperimentalAtomicApi::class)
private data class RefCountedSink(
    val value: Sink
) {
    private var refCount: AtomicInt = AtomicInt(0)

    @Suppress("NOTHING_TO_INLINE")
    inline fun acquire(): RefCountedSink {
        refCount.incrementAndFetch()
        return this
    }

    inline fun release(releaseAction: () -> Unit = {}): RefCountedSink {
        if (refCount.load() == 0) return this
        refCount.decrementAndFetch()
        if (refCount.load() == 0) {
            releaseAction()
            value.close()
            return this
        }
        return this
    }
}

@PublishedApi
internal class FileAppender( // @formatter:off
    override val pattern: String,
    override val formatter: Formatter,
    private val path: Path,
    private val filter: Filter
) : Appender { // @formatter:on
    companion object {
        private val sinks: SharedHashMap<Path, RefCountedSink> = SharedHashMap()
    }

    private val sink: RefCountedSink = sinks.getOrPut(path) {
        RefCountedSink(SystemFileSystem.sink(path).buffered())
    }.acquire()

    private val mutex: Mutex = Mutex()

    override fun append(logger: Logger, level: Level, message: String, marker: Marker?) {
        if (!filter(level, message, marker)) return
        // Make sure to strip out any ANSI codes when writing to file
        mutex.withBlockingLock {
            sink.value.writeString("${message.toAnsi().cleanString()}\n")
        }
    }

    override fun dispose() {
        sink.release { sinks -= path }
    }
}