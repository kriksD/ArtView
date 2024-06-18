package properties.data

import info.ImageInfo
import TagCategory
import getImageDimensions
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import properties.Properties
import uniqueId
import java.io.File


class DataContainer {
    var data: Data = Data()

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    private val file = File("imagesData.json")

    fun load() {
        loadFromJsonFile()
        removeNonexistentImages()
        checkForNewImageFiles()
        ensureDefaultTagsExist()
        save()
    }

    private fun loadFromJsonFile() {
        if (!file.exists()) return

        try {
            data = json.decodeFromString(file.readText())

        } catch (e: Exception) {
            val converter = DataConverter(file)
            data = converter.loadAndConvert() ?: Data()
        }
    }

    private fun removeNonexistentImages() {
        data.images.forEach {
            if (!File(it.path).exists()) {
                it.delete()
                data.images.remove(it)
            }
        }
    }

    private fun checkForNewImageFiles() {
        val allFiles = File("images").listFiles()
        allFiles?.filter { f ->
            data.images.none { it.path == f.path }
        }?.forEach {
            val dimensions = getImageDimensions(it.path) ?: return@forEach
            data.images.add(0, ImageInfo(data.images.uniqueId(), it.path, dimensions.width, dimensions.height, it.name))
        }
    }

    private fun ensureDefaultTagsExist() {
        if (!data.containsTagCategory("Other")) {
            data.tags.add(TagCategory("Other", mutableListOf("NSFW")))
        }
    }

    fun save() {
        data.meta.version = Properties.DATA_VERSION
        file.writeText(json.encodeToString(data))
    }
}