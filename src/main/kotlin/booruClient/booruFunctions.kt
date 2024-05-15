package booruClient

import capitalizeEachWord

fun splitTags(tagString: String, capitalize: Boolean = false): List<String> {
    if (tagString.isBlank()) return listOf()

    return tagString
        .split(" ")
        .map { it.replace("_", " ") }
        .let { if (capitalize) it.map { tag -> tag.capitalizeEachWord() } else it }
}