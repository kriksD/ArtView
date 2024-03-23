package properties.data

import kotlinx.serialization.Serializable
import properties.Properties

@Serializable
data class DataMeta(
    val version: String = Properties.dataVersion,
)
