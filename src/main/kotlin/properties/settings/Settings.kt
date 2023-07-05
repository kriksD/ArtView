package properties.settings

import kotlinx.serialization.*

@Serializable
data class Settings(
    var language: String = "en",
    var background: String = "bg1.png",
    val selected_tags_by_default: MutableList<String> = mutableListOf(),
    val anti_selected_tags_by_default: MutableList<String> = mutableListOf("NSFW"),
    var auto_select_created_tags: Boolean = false,
)