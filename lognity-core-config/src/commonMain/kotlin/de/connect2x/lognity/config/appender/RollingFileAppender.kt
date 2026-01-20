package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableFilter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import de.connect2x.lognity.appender.RollingFileAppender as RollingFileAppenderImpl

@SerialName("rolling_file")
@Serializable
data class RollingFileAppender(
    override val pattern: String,
    override val formatter: String,
    @SerialName("base_path") val basePath: String,
    override val filter: SerializableFilter = SerializableFilter(),
    override val name: String? = null,
    @SerialName("file_count") val fileCount: Int = RollingFileAppenderImpl.DEFAULT_FILE_COUNT,
    @SerialName("max_file_size") val maxFileSize: Long = RollingFileAppenderImpl.DEFAULT_FILE_SIZE,
    @SerialName("use_timestamps") val useTimestamps: Boolean = true,
    @SerialName("delete_existing") val deleteExisting: Boolean = false,
    @SerialName("latest_suffix") val latestSuffix: String = RollingFileAppenderImpl.DEFAULT_LATEST_SUFFIX
) : SerializableAppender