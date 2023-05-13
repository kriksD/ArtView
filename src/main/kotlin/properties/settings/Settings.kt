package properties.settings

import kotlinx.serialization.*

@Serializable
data class Settings(
    var language: String = "en",
    var background: String = "bg1.png",
)