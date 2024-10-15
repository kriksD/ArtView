package tag

import kotlinx.serialization.Serializable

@Serializable
data class TagCategory(
    var name: String,
    val tags: MutableList<String> = mutableListOf(),
)