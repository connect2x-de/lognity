package de.connect2x.lognity.io

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class AsyncSinkTest {
    private fun getTestPath(name: String) =
        Path("test_async_sink_${name}_${Clock.System.now().toEpochMilliseconds()}.log")

    private fun cleanup(path: Path) {
        if (SystemFileSystem.exists(path)) {
            SystemFileSystem.delete(path)
        }
    }

    @Test
    fun `Write and flush`() {
        val path = getTestPath("basic")
        val sink = AsyncSink(path)
        try {
            sink.write("Hello, World!")
            sink.flush()
            sink.close()

            assertTrue(SystemFileSystem.exists(path), "File should exist after close")
            val result = SystemFileSystem.source(path).buffered().use { it.readString() }
            assertEquals("Hello, World!", result)
        }
        finally {
            cleanup(path)
        }
    }

    @Test
    fun `Delete existing file`() {
        val path = getTestPath("delete_existing")
        SystemFileSystem.sink(path).buffered().use { it.writeString("Existing Content") }

        val sink = AsyncSink(path, deleteExisting = true)
        try {
            sink.write("New Content")
            sink.close()

            val result = SystemFileSystem.source(path).buffered().use { it.readString() }
            assertEquals("New Content", result)
        }
        finally {
            cleanup(path)
        }
    }

    @Test
    fun `Keep existing file`() {
        val path = getTestPath("keep_existing")
        SystemFileSystem.sink(path).buffered().use { it.writeString("Existing Content") }

        val sink = AsyncSink(path, deleteExisting = false)
        try {
            sink.write(" - New Content")
            sink.close()

            val result = SystemFileSystem.source(path).buffered().use { it.readString() }
            assertEquals("Existing Content - New Content", result)
        }
        finally {
            cleanup(path)
        }
    }

    @Test
    fun `Concurrent writes`() {
        val path = getTestPath("concurrent")
        val sink = AsyncSink(path)
        try {
            val expected = StringBuilder()

            for (i in 1..100) {
                val message = "Message $i\n"
                expected.append(message)
                sink.write(message)
            }

            sink.close()

            val result = SystemFileSystem.source(path).buffered().use { it.readString() }
            assertEquals(expected.toString(), result)
        }
        finally {
            cleanup(path)
        }
    }
}
