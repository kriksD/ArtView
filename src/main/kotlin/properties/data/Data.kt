package properties.data

import info.ImageGroup
import info.ImageInfo
import TagCategory
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import containsAtLeastOne
import getFirstFrame
import getImageBitmap
import kotlinx.serialization.Serializable
import properties.Properties
import savePngTo
import swap
import toFileName
import uniqueId
import uniqueName
import java.io.File

@Serializable(with = DataSerializer::class)
class Data(
    val meta: DataMeta = DataMeta(),
    tags: Collection<TagCategory> = mutableListOf(TagCategory("Other", mutableListOf("NSFW"))),
    images: Collection<ImageInfo> = mutableListOf(),
    imageGroups: Collection<ImageGroup> = mutableListOf(),
) {
    val tags = tags.toMutableStateList()
    val images = images.toMutableStateList()
    val imageGroups = imageGroups.toMutableStateList()

    fun addImage(file: File): ImageInfo? {
        if (!file.exists()) return null
        val image = getImageBitmap(file) ?: getFirstFrame(file) ?: return null

        val newFile = File("data/images/${
            uniqueName(file.nameWithoutExtension, file.extension, File("data/images"))
        }.${file.extension}")

        file.copyTo(newFile)

        return ImageInfo(
            images.uniqueId(),
            newFile.path,
            image.width,
            image.height,
            newFile.name,
        ).also {
            images.add(0, it)
        }
    }

    fun addImage(path: String): ImageInfo? = addImage(File(path))

    fun addImage(image: ImageBitmap, name: String = "new_image"): ImageInfo {
        val newFile = File("data/images/${uniqueName(name.ifBlank { "new_image" }.toFileName(), "png", File("data/images"))}.png")
        image.savePngTo(newFile)

        return ImageInfo(
            images.uniqueId(),
            newFile.path,
            image.width,
            image.height,
            newFile.name,
        ).also {
            images.add(0, it)
        }
    }

    fun delete(imagesForDeletion: List<ImageInfo>) {
        images.removeAll(imagesForDeletion)
        imageGroups.forEach { ig -> ig.imagePaths.removeAll(imagesForDeletion.map { it.path }) }
        imagesForDeletion.forEach { it.delete() }
        Properties.saveData()
    }

    fun deleteGroups(imagesForDeletion: List<ImageGroup>) {
        imageGroups.removeAll(imagesForDeletion)
        Properties.saveData()
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

    fun countSize(): Long {
        var sizeOfImageInfoInBytes = 0L

        images.forEach { image ->
            sizeOfImageInfoInBytes += image.name.encodeToByteArray().size
            sizeOfImageInfoInBytes += image.path.encodeToByteArray().size
            sizeOfImageInfoInBytes += image.description.encodeToByteArray().size

            image.tags.forEach {
                sizeOfImageInfoInBytes += it.encodeToByteArray().size
            }

            sizeOfImageInfoInBytes += 1 // width
            sizeOfImageInfoInBytes += 1 // height
        }

        return sizeOfImageInfoInBytes
    }

    fun countImageSize(): Long = images
        .filter { it.isLoaded }
        .sumOf { it.scaledDownImage!!.toPixelMap().buffer.size * 16L }

    fun moveCategoryUp(name: String) {
        val index = tags.indexOfFirst { it.name == name }
        tags.swap(index, index - 1)
    }

    fun moveCategoryDown(name: String) {
        val index = tags.indexOfFirst { it.name == name }
        tags.swap(index, index + 1)
    }

    fun copy(
        meta: DataMeta = this.meta,
        tags: Collection<TagCategory> = this.tags,
        images: Collection<ImageInfo> = this.images,
        imageGroups: Collection<ImageGroup> = this.imageGroups,
    ): Data {
        return Data(
            meta,
            tags.toMutableList(),
            images.toMutableList(),
            imageGroups.toMutableList(),
        )
    }
}