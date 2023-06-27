package properties.data

import ImageInfo
import TagCategory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        data = tryToLoadData() ?: tryToLoadOldData() ?: Data()
    }

    private fun tryToLoadData(): Data? {
        return try {
            if (file.exists()) {
                data.images.removeIf { !File(it.path).exists() }
                json.decodeFromString(file.readText())

            } else { null }
        } catch (e: Exception) { null }
    }

    private fun tryToLoadOldData(): Data? {
        return try {
            if (file.exists()) {
                val oldData = json.decodeFromString<OldData>(file.readText())
                data.images.removeIf { !File(it.path).exists() }
                Data(
                    mutableListOf(TagCategory("Other", oldData.tags)),
                    oldData.images,
                )

            } else { null }
        } catch (e: Exception) { null }
    }

    private fun removeNonexistentImages() {
        data.images.removeIf { !File(it.path).exists() }
    }

    private fun checkForNewImageFiles() {
        val allFiles = File("images").listFiles()
        allFiles?.filter { f ->
            data.images.none { it.path == f.path }
        }?.forEach {
            data.images.add(0, ImageInfo(it.path, it.name))
        }
    }

    private fun ensureDefaultTagsExist() {
        if (!data.containsTagCategory("Other")) {
            data.tags.add(TagCategory("Other", mutableListOf("NSFW")))
        }

        if (!data.containsTag("NSFW")) {
            data.findTagCategory("Other")?.tags?.add("NSFW")
        }
    }

    fun save() {
        file.writeText(json.encodeToString(data))
    }
}