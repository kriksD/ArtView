package properties.data

import info.MediaInfo
import TagCategory
import getImageDimensions
import getVideoDimensions
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
    private val file = File("data/images_data.json")
    private val imagesFolder = File("data/images")

    fun load() {
        loadFromJsonFile()
        checkAndFixPaths()
        removeNonexistentImages()
        checkForNewImageFiles()
        ensureDefaultTagsExist()
        save()
    }

    private fun loadFromJsonFile() {
        if (!file.exists()) return
        data = try { json.decodeFromString(file.readText()) } catch (e: Exception) { Data() }
    }

    private fun checkAndFixPaths() {
        val wrongPathRegex = Regex("^images[/\\\\].+\\..+$")
        if (
            data.mediaList.any { it.path.contains(wrongPathRegex) }
            || data.mediaGroups.any { g -> g.paths.any { it.contains(wrongPathRegex) } }
        ) {
            val newImages = data.mediaList.map {
                if (it.path.contains(wrongPathRegex)) {
                    it.copy(path = "data${File.separator}${it.path}")
                } else {
                    it
                }
            }

            val newGroups = data.mediaGroups.map {
                val newPaths = it.paths.map { path ->
                    if (path.contains(wrongPathRegex)) {
                        "data${File.separator}${path}"
                    } else {
                        path
                    }
                }
                it.copy(paths = newPaths.toMutableList())
            }

            data = data.copy(mediaList = newImages, mediaGroups = newGroups)
        }
    }

    private fun removeNonexistentImages() {
        data.mediaList.forEach {
            if (!File(it.path).exists()) {
                it.delete()
                data.mediaList.remove(it)
            }
        }
    }

    private fun checkForNewImageFiles() {
        val allFiles = imagesFolder.listFiles()
        allFiles?.filter { f ->
            data.mediaList.none { it.path == f.path }
        }?.forEach {
            val dimensions = getImageDimensions(it.path) ?: getVideoDimensions(it.path) ?: return@forEach
            data.mediaList.add(0, MediaInfo(data.mediaList.uniqueId(), it.path, dimensions.width, dimensions.height, it.name))
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