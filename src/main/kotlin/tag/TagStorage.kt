package tag

import androidx.compose.runtime.*
import settings
import tagData

class TagStorage {
    val selectedTags = mutableStateListOf<String>()
    val selectedAntiTags = mutableStateListOf<String>()

    val filteredTags = mutableStateMapOf<String, List<String>>()

    fun filter(filterString: String = "", categoryName: String? = null) {
        if (filterString.isEmpty()) {
            filteredTags.clear()
            tagData.tags.forEach { filteredTags[it.name] = it.tags }
            return
        }

        if (categoryName != null) {
            val category = tagData.findTagCategory(categoryName) ?: return
            filteredTags[categoryName] = category.tags.filter { it.contains(filterString) }

        } else {
            val allTags = tagData.tags
            allTags.forEach { category ->
                filteredTags[category.name] = category.tags.filter { it.contains(filterString) }
            }
        }
    }

    fun changeSelectStatus(tag: String) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag)
            selectedAntiTags.add(tag)

        } else if (selectedAntiTags.contains(tag)) {
            selectedTags.remove(tag)
            selectedAntiTags.remove(tag)

        } else {
            selectedTags.add(tag)
            selectedAntiTags.remove(tag)
        }
    }

    fun isSelected(tag: String) = selectedTags.contains(tag)

    fun isAntiSelected(tag: String) = selectedAntiTags.contains(tag)

    fun reset() {
        selectedTags.clear()
        selectedTags.addAll(settings.selectedTagsByDefault)
        selectedAntiTags.clear()
        selectedAntiTags.addAll(settings.antiSelectedTagsByDefault)
        filteredTags.clear()
        tagData.tags.forEach { filteredTags[it.name] = it.tags }
    }
}