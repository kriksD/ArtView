package properties.data

import androidx.compose.runtime.mutableStateListOf
import tag.TagCategory
import androidx.compose.runtime.toMutableStateList
import containsAtLeastOne
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import properties.Properties
import swap
import java.io.File

@Serializable(with = TagDataSerializer::class)
class TagData(
    var dataVersion: String = Properties.DATA_VERSION,
    tags: Collection<TagCategory> = mutableListOf(TagCategory("Other", mutableStateListOf("NSFW"))),
) {
    val tags = tags.toMutableStateList()

    fun containsTag(tag: String): Boolean {
        tags.forEach { category ->
            if (category.tags.contains(tag)) return true
        }

        return false
    }

    fun addTag(tag: String, category: String = "Other") {
        if (containsTag(tag)) return
        val tagsList = tags.find { it.name == category }?.tags
        tagsList?.add(tag)
        tagsList?.sort()
    }

    fun addAllTags(list: List<String>, category: String = "Other") {
        val tagsList = tags.find { it.name == category }?.tags
        tagsList?.addAll(list.filter { !tagsList.contains(it) })
        tagsList?.sort()
    }

    fun addNewTags(
        tags: List<String>,
        selectedTags: List<String>,
        categoryName: String?,
    ) {
        if (tags.isEmpty() || !selectedTags.containsAtLeastOne(tags) || categoryName == null) return

        if (!containsTagCategory(categoryName)) {
            createCategory(categoryName)
        }

        addAllTags(tags.filter { selectedTags.contains(it) }, categoryName)
    }

    fun addTagsFromFile(file: File) {
        if (!file.exists()) return

        val loadedTags = Json.decodeFromString<TagData>(file.readText())

        loadedTags.tags.forEach { category ->
            createCategory(category.name)

            category.tags.forEach { tag ->
                addTag(tag, category.name)
            }
        }
    }

    fun moveTag(tag: String, category: String) {
        if (!containsTagCategory(category)) return

        removeTag(tag)
        addTag(tag, category)
    }

    fun removeTag(tag: String) {
        tags.forEach { it.tags.remove(tag) }
    }

    fun containsTagCategory(name: String): Boolean = tags.find { it.name == name } != null

    fun findTagCategory(name: String): TagCategory? = tags.find { it.name == name }

    fun createCategory(name: String) {
        if (tags.none { it.name == name }) {
            tags.add(TagCategory(name))
        }
    }

    fun removeCategory(name: String) {
        if (name == "Other") return

        findTagCategory(name)?.let { category ->
            if (category.tags.isNotEmpty()) {
                addAllTags(category.tags)
            }

            tags.remove(category)
        }
    }

    fun moveCategoryUp(name: String) {
        val index = tags.indexOfFirst { it.name == name }
        tags.swap(index, index - 1)
    }

    fun moveCategoryDown(name: String) {
        val index = tags.indexOfFirst { it.name == name }
        tags.swap(index, index + 1)
    }

    fun copy(
        dataVersion: String = this.dataVersion,
        tags: Collection<TagCategory> = this.tags,
    ): TagData {
        return TagData(
            dataVersion,
            tags.toMutableList(),
        )
    }
}