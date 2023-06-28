package properties.data

import ImageInfo
import TagCategory
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable
import properties.Properties
import saveWebPTo
import uniqueName
import java.io.File

@Serializable
data class Data(
    val tags: MutableList<TagCategory> = mutableListOf(TagCategory("Other", mutableListOf("NSFW"))),
    val images: MutableList<ImageInfo> = mutableListOf(),
) {
    fun addImage(file: File): ImageInfo? {
        if (!file.exists()) return null

        val newFile = File("images/${
            uniqueName(file.nameWithoutExtension, file.extension, File("images"))
        }.${file.extension}")

        file.copyTo(newFile)

        return ImageInfo(
            newFile.path,
            newFile.name,
        ).also {
            images.add(0, it)
        }
    }

    fun addImage(path: String): ImageInfo? = addImage(File(path))

    fun addImage(image: ImageBitmap): ImageInfo {
        val newFile = File("images/${uniqueName("new_image", ".png", File("images"))}.png")
        image.saveWebPTo(newFile)

        return ImageInfo(
            newFile.path,
            newFile.name,
        ).also {
            images.add(0, it)
        }
    }

    fun delete(imageInfo: ImageInfo) {
        val found = images.find { it.path == imageInfo.path }
        found?.let {
            images.remove(it)
            it.delete()
        }
    }

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

    fun moveTag(tag: String, category: String) {
        if (!containsTagCategory(category)) return

        removeTag(tag)
        addTag(tag, category)
    }

    fun removeTag(tag: String) {
        if (tag == "NSFW") return
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
}