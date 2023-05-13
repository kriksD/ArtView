package properties

import ImageInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class DataContainer {
    lateinit var data: Data

    val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    private val file = File("imagesData.json")

    fun load() {
        try {
            if (file.exists()) {
                data = json.decodeFromString(file.readText())
                data.images.removeIf { !File(it.path).exists() }

            } else { data = Data() }
        } catch (e: Exception) { data = Data() }

        checkForNewImageFiles()
        checkTags()
        save()
    }

    private fun checkForNewImageFiles() {
        val allFiles = File("images").listFiles()
        allFiles?.filter { f ->
            data.images.none { it.path == f.path }
        }?.forEach {
            data.images.add(ImageInfo(it.path, it.name))
        }
    }

    private fun checkTags() {
        if (!data.tags.contains("NSFW")) {
            data.tags.add("NSFW")
        }
    }

    fun save() {
        file.writeText(json.encodeToString(data))
    }
}