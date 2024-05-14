package properties.data

import kotlinx.serialization.Serializable
import properties.Properties

@Serializable
data class DataMeta(
    var version: String = Properties.dataVersion,
)
