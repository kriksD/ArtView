package properties.data

import tag.TagCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import properties.DataFolder
import properties.Properties
import java.io.File


class DataContainer {
    var mediaData: MediaData = MediaData()
    var tagData: TagData = TagData()

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    fun load() {
        loadFromJsonFile()
        removeNonexistentMedia()
        checkForNewFiles()
        ensureDefaultTagsExist()
        save()
    }

    private fun loadFromJsonFile() {
        val mediaFile = DataFolder.mediaDataFile

        if (!mediaFile.exists()) return
        mediaData = try {
            json.decodeFromString(mediaFile.readText())

        } catch (e: Exception) {
            e.printStackTrace()
            MediaData()
        }

        val tagFile = DataFolder.tagDataFile
        if (!tagFile.exists()) return
        tagData = try {
            json.decodeFromString(tagFile.readText())

        } catch (e: Exception) {
            e.printStackTrace()
            TagData()
        }
    }

    private fun removeNonexistentMedia() {
        mediaData.mediaList.forEach {
            if (!File(it.path).exists()) {
                mediaData.delete(listOf(it))
            }
        }
    }

    private fun checkForNewFiles() {
        val allFiles = (DataFolder.imageFolder.listFiles() ?: emptyArray<File>()) +
                (DataFolder.gifFolder.listFiles() ?: emptyArray<File>()) +
                (DataFolder.videoFolder.listFiles() ?: emptyArray<File>()) +
                (DataFolder.audioFolder.listFiles() ?: emptyArray<File>())

        allFiles.filter { f ->
            mediaData.mediaList.none { it.path == f.path }
        }.forEach {
            mediaData.addMedia(it.path, createCopy = false)
        }
    }

    private fun ensureDefaultTagsExist() {
        if (!tagData.containsTagCategory("Other")) {
            tagData.tags.add(TagCategory("Other", mutableListOf("NSFW")))
        }
    }

    fun save() {
        DataFolder.imageFolder.mkdirs()
        DataFolder.gifFolder.mkdirs()
        DataFolder.videoFolder.mkdirs()
        DataFolder.audioFolder.mkdirs()

        mediaData.dataVersion = Properties.DATA_VERSION
        DataFolder.mediaDataFile.writeText(json.encodeToString(mediaData))

        tagData.dataVersion = Properties.DATA_VERSION
        DataFolder.tagDataFile.writeText(json.encodeToString(tagData))
    }
}