package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableFilter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("rolling_file")
@Serializable
data class RollingFileAppender(
    override val pattern: String,
    override val formatter: String,
    @SerialName("base_path") val basePath: String,
    override val filter: SerializableFilter = SerializableFilter(),
    override val name: String? = null,
    @SerialName("file_count") val fileCount: Int = 10,
    @SerialName("max_file_size") val maxFileSize: Long = 1024 * 1024, // 1MB per default
    @SerialName("use_timestamps") val useTimestamps: Boolean = true,
    @SerialName("delete_existing") val deleteExisting: Boolean = false
) : SerializableAppender