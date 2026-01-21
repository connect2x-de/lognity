package de.connect2x.lognity.config.appender

import de.connect2x.lognity.config.SerializableFilter
import de.connect2x.lognity.config.serialization.RefOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import de.connect2x.lognity.appender.RollingFileAppender as RollingFileAppenderImpl

@SerialName("rolling_file")
@Serializable
data class RollingFileAppender(
    override val pattern: RefOrValue<String>,
    override val formatter: RefOrValue<String>,
    @SerialName("base_path") val basePath: RefOrValue<String>,
    override val filter: RefOrValue<SerializableFilter> = RefOrValue.Value(SerializableFilter()),
    override val name: RefOrValue<String?> = RefOrValue.Value(null),
    @SerialName("file_count") val fileCount: RefOrValue<Int> = RefOrValue.Value(RollingFileAppenderImpl.DEFAULT_FILE_COUNT),
    @SerialName("max_file_size") val maxFileSize: RefOrValue<Long> = RefOrValue.Value(RollingFileAppenderImpl.DEFAULT_FILE_SIZE),
    @SerialName("use_timestamps") val useTimestamps: RefOrValue<Boolean> = RefOrValue.Value(true),
    @SerialName("delete_existing") val deleteExisting: RefOrValue<Boolean> = RefOrValue.Value(false),
    @SerialName("latest_suffix") val latestSuffix: RefOrValue<String> = RefOrValue.Value(RollingFileAppenderImpl.DEFAULT_LATEST_SUFFIX)
) : SerializableAppender