package de.connect2x.lognity.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

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

    private fun assertRollingContents(
        name: String,
        messages: List<String>,
        expectedFiles: Map<String, String>,
        fileCount: Int = 5,
        latestSuffix: String = "-latest",
    ) {
        val basePath = getTestBasePath(name)
        try {
            RollingAsyncSink(
                basePath,
                fileCount = fileCount,
                maxFileSize = 10L,
                useTimestamps = false,
                deleteExisting = true,
                latestSuffix = latestSuffix,
            ).use { sink ->
                messages.forEach(sink::write)
            }

            val files = SystemFileSystem.list(basePath.parent ?: Path("."))
            val actualFiles = files.associate { file ->
                file.name to SystemFileSystem.source(file).buffered().use { it.readString() }
            }
            assertEquals(expectedFiles, actualFiles)
        } finally {
            cleanup(basePath)
        }
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
            latestSuffix = "-latest",
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
        } finally {
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
            latestSuffix = "-latest",
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

        } finally {
            cleanup(basePath)
        }
    }

    @Test
    fun `No rolling below maxFileSize`() {
        assertRollingContents(
            name = "below_limit",
            messages = listOf("0123", "45678"),
            expectedFiles = mapOf("test.0-latest.log" to "012345678"),
        )
    }

    @Test
    fun `Rolling on cumulative maxFileSize`() {
        assertRollingContents(
            name = "cumulative_exact",
            messages = listOf("0123", "456789"),
            expectedFiles = mapOf(
                "test.0.log" to "0123456789",
                "test.1-latest.log" to "",
            ),
        )
    }

    @Test
    fun `Oversized write rolls once and resets byte count`() {
        assertRollingContents(
            name = "oversized",
            messages = listOf("012345678901234567890", "123456789"),
            expectedFiles = mapOf(
                "test.0.log" to "012345678901234567890",
                "test.1-latest.log" to "123456789",
            ),
        )
    }

    @Test
    fun `Rolling when cumulative writes exceed maxFileSize`() {
        assertRollingContents(
            name = "cumulative_overshoot",
            messages = listOf("012345", "6789a", "abcdef", "ghijk", "tail"),
            expectedFiles = mapOf(
                "test.0.log" to "0123456789a",
                "test.1.log" to "abcdefghijk",
                "test.2-latest.log" to "tail",
            ),
        )
    }

    @Test
    fun `Rolling uses UTF-8 byte count rather than character count`() {
        assertRollingContents(
            name = "utf8_overshoot",
            messages = listOf("ééé", "ééé"),
            expectedFiles = mapOf(
                "test.0.log" to "éééééé",
                "test.1-latest.log" to "",
            ),
        )
    }

    @Test
    fun `File count limit and overwriting`() {
        assertRollingContents(
            name = "limit",
            messages = listOf("first______", "second______", "third________", "fourth________"),
            expectedFiles = mapOf(
                "test.0.log" to "fourth________",
                "test.1-latest.log" to "",
                "test.2.log" to "third________",
            ),
            fileCount = 3,
        )
    }

    @Test
    fun `No latest suffix`() {
        assertRollingContents(
            name = "no_suffix",
            messages = listOf("0123456789a", "abcdefghijk"),
            expectedFiles = mapOf(
                "test.0.log" to "",
                "test.1.log" to "abcdefghijk",
            ),
            fileCount = 2,
            latestSuffix = "",
        )
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
            latestSuffix = "-latest",
        )
        try {
            sink.write("Timestamped")
            sink.close()

            val parentDir = basePath.parent ?: Path(".")
            val files = SystemFileSystem.list(parentDir).toList()
            assertEquals(1, files.size)
            assertTrue(files[0].name.contains("-latest"), "Should contain latest suffix")
            assertTrue(files[0].name.contains("20"), "Should contain part of year")
        } finally {
            cleanup(basePath)
        }
    }
}
