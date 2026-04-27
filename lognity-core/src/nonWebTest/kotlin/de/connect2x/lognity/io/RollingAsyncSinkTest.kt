package de.connect2x.lognity.io

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class RollingAsyncSinkTest {
    private fun getTestBasePath(name: String): Path {
        val workingDir = SystemFileSystem.resolve(Path("."))
        val directory = Path(workingDir, "test_rolling_async_sink_${name}_${Clock.System.now().toEpochMilliseconds()}")
        SystemFileSystem.createDirectories(directory)
        return Path(directory, "test.log")
    }

    private fun cleanup(basePath: Path) {
        val parentDir = basePath.parent ?: return
        SystemFileSystem.list(parentDir).forEach(SystemFileSystem::delete)
        SystemFileSystem.delete(parentDir, mustExist = false)
    }

    @Test
    fun `Basic write and flush`() {
        val basePath = getTestBasePath("basic")
        val sink = RollingAsyncSink(
            basePath,
            fileCount = 1,
            maxFileSize = 1024,
            useTimestamps = false,
            deleteExisting = true,
            latestSuffix = "-latest"
        )
        try {
            sink.write("Hello, Rolling World!")
            sink.flush()
            sink.close()

            val parentDir = basePath.parent ?: Path(".")
            val files = SystemFileSystem.list(parentDir).toList()
            assertEquals(1, files.size, "Should have exactly one log file")

            val result = SystemFileSystem.source(files[0]).buffered().use { it.readString() }
            assertEquals("Hello, Rolling World!", result)
        }
        finally {
            cleanup(basePath)
        }
    }

    @Test
    fun `Rolling on maxFileSize`() {
        val basePath = getTestBasePath("roll")
        val sink = RollingAsyncSink(
            basePath,
            fileCount = 5,
            maxFileSize = 10L,
            useTimestamps = false,
            deleteExisting = true,
            latestSuffix = "-latest"
        )
        try {
            // Write 3 chunks of 10 bytes each
            sink.write("0123456789")
            sink.write("abcdefghij")
            sink.write("klmnopqrst")
            sink.close()

            val parentDir = basePath.parent ?: Path(".")
            val files = SystemFileSystem.list(parentDir)

            // Should have 4 files (3 rolled + 1 active empty)
            assertEquals(4, files.size, "Should have 4 files (3 rolled + 1 active empty)")

            // Verify contents
            val file0 = files.find { it.name.contains(".0.") && !it.name.contains("-latest") }!!
            val file1 = files.find { it.name.contains(".1.") && !it.name.contains("-latest") }!!
            val file2 = files.find { it.name.contains(".2.") && !it.name.contains("-latest") }!!
            val file3Latest = files.find { it.name.contains(".3-latest") }!!

            assertEquals("0123456789", SystemFileSystem.source(file0).buffered().use { it.readString() })
            assertEquals("abcdefghij", SystemFileSystem.source(file1).buffered().use { it.readString() })
            assertEquals("klmnopqrst", SystemFileSystem.source(file2).buffered().use { it.readString() })
            assertEquals("", SystemFileSystem.source(file3Latest).buffered().use { it.readString() })

        }
        finally {
            cleanup(basePath)
        }
    }

    @Test
    fun `File count limit and overwriting`() {
        val basePath = getTestBasePath("limit")
        val fileCount = 3
        val sink = RollingAsyncSink(
            basePath,
            fileCount = fileCount,
            maxFileSize = 10L,
            useTimestamps = false,
            deleteExisting = true,
            latestSuffix = "-latest"
        )
        try {
            sink.write("first_____")
            sink.write("second____")
            sink.write("third_____")
            sink.write("first_____")
            sink.close()

            val parentDir = basePath.parent ?: Path(".")
            val files = SystemFileSystem.list(parentDir)

            assertEquals(fileCount, files.size, "Should have 3 files")
        }
        finally {
            cleanup(basePath)
        }
    }

    @Test
    fun `No latest suffix`() {
        val basePath = getTestBasePath("no_suffix")
        val sink = RollingAsyncSink(
            basePath, fileCount = 2, maxFileSize = 10L, useTimestamps = false, deleteExisting = true, latestSuffix = ""
        )
        try {
            sink.write("0123456789")
            sink.write("abcdefghij")
            sink.close()

            val parentDir = basePath.parent ?: Path(".")
            val files = SystemFileSystem.list(parentDir)

            assertEquals(2, files.size)
            val file0 = files.find { it.name.endsWith(".0.log") }!!
            val file1 = files.find { it.name.endsWith(".1.log") }!!

            assertEquals("", SystemFileSystem.source(file0).buffered().use { it.readString() })
            assertEquals("abcdefghij", SystemFileSystem.source(file1).buffered().use { it.readString() })
        }
        finally {
            cleanup(basePath)
        }
    }

    @Test
    fun `Use timestamps`() {
        val basePath = getTestBasePath("timestamps")
        val sink = RollingAsyncSink(
            basePath,
            fileCount = 1,
            maxFileSize = 1000,
            useTimestamps = true,
            deleteExisting = true,
            latestSuffix = "-latest"
        )
        try {
            sink.write("Timestamped")
            sink.close()

            val parentDir = basePath.parent ?: Path(".")
            val files = SystemFileSystem.list(parentDir).toList()
            assertEquals(1, files.size)
            assertTrue(files[0].name.contains("-latest"), "Should contain latest suffix")
            assertTrue(files[0].name.contains("20"), "Should contain part of year")
        }
        finally {
            cleanup(basePath)
        }
    }
}
