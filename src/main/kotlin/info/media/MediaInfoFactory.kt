package info.media

import getAudioArtist
import getAudioCover
import getAudioDuration
import getAudioTitle
import getImageDimensions
import getVideoDimensions
import getVideoDuration
import info.media.serializers.MediaInfoSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import properties.DataFolder
import uniqueName
import utilities.HasID
import utilities.multipleUniqueIDs
import utilities.uniqueId
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

class MediaInfoFactory {
    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
        }

        fun makeFromFile(
            file: File,
            ids: List<HasID> = listOf(),
            createCopy: Boolean = true,
        ): MediaInfo? {
            if (!file.exists()) return null

            val type = MediaType.determineType(file) ?: return null
            val newFile = createFile(file, type, createCopy)

            when (type) {
                MediaType.Image -> {
                    val dimensions = getImageDimensions(newFile) ?: return null
                    return ImageInfo(
                        id = ids.uniqueId(),
                        path = newFile.path,
                        name = newFile.name,
                        width = dimensions.width,
                        height = dimensions.height,
                    )
                }
                MediaType.GIF -> {
                    val dimensions = getImageDimensions(newFile) ?: return null
                    return GIFInfo(
                        id = ids.uniqueId(),
                        path = newFile.path,
                        name = newFile.name,
                        width = dimensions.width,
                        height = dimensions.height,
                    )
                }
                MediaType.Audio -> {
                    val cover = getAudioCover(newFile)
                    val artist = getAudioArtist(file)
                    val title = getAudioTitle(file)

                    val newName = when {
                        artist != null && title != null -> "$artist - $title"
                        artist != null -> artist
                        title != null -> title
                        else -> newFile.name
                    }

                    return AudioInfo(
                        id = ids.uniqueId(),
                        path = newFile.path,
                        name = newName,
                        duration = getAudioDuration(file) ?: return null,
                        thumbnailID = null,
                        thumbnailWidth = cover?.width,
                        thumbnailHeight = cover?.height,
                    )
                }
                MediaType.Video -> {
                    val dimensions = getVideoDimensions(newFile) ?: return null
                    return VideoInfo(
                        id = ids.uniqueId(),
                        path = newFile.path,
                        name = newFile.name,
                        width = dimensions.width,
                        height = dimensions.height,
                        duration = getVideoDuration(file) ?: return null,
                    )
                }
            }
        }

        fun makeFromFile(
            path: String,
            ids: List<HasID> = listOf(),
            withoutCopy: Boolean = false,
        ): MediaInfo? = makeFromFile(File(path), ids, withoutCopy)

        private fun createName(file: File, type: MediaType, createCopy: Boolean): String {
            return if (createCopy) {
                when (type) {
                    MediaType.Image -> uniqueName(file.nameWithoutExtension, file.extension, DataFolder.imageFolder)
                    MediaType.GIF -> uniqueName(file.nameWithoutExtension, file.extension, DataFolder.gifFolder)
                    MediaType.Video -> uniqueName(file.nameWithoutExtension, file.extension, DataFolder.videoFolder)
                    MediaType.Audio -> uniqueName(file.nameWithoutExtension, file.extension, DataFolder.audioFolder)
                }
            } else {
                file.nameWithoutExtension
            }
        }

        private fun createFile(file: File, type: MediaType, createCopy: Boolean): File {
            val name = createName(file, type, createCopy)

            return if (createCopy) {
                when (type) {
                    MediaType.Image -> DataFolder.imageFolder.resolve("$name.${file.extension}")
                    MediaType.GIF -> DataFolder.gifFolder.resolve("$name.${file.extension}")
                    MediaType.Video -> DataFolder.videoFolder.resolve("$name.${file.extension}")
                    MediaType.Audio -> DataFolder.audioFolder.resolve("$name.${file.extension}")
                }
            } else {
                file

            }.also {
                if (createCopy) {
                    file.copyTo(it)
                }
            }
        }

        fun createFromZip(
            file: File,
            ids: List<HasID> = listOf(),
        ): List<MediaInfo>? {
            if (ZipFile(file).getEntry("media_data.json") == null) return null

            var data: MutableList<MediaInfo>? = null
            val newPaths: MutableList<Pair<File, File>> = mutableListOf()

            ZipInputStream(FileInputStream(file)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "media_data.json" -> data = json.decodeFromString(ListSerializer(MediaInfoSerializer), zip.readBytes().decodeToString()).toMutableList()
                        else -> {
                            var potentialFile = DataFolder.mediaFolder.resolve(entry.name)

                            if (potentialFile.exists()) {
                                val newName = uniqueName(potentialFile.nameWithoutExtension, potentialFile.extension, potentialFile.parentFile)
                                val newFile = potentialFile.parentFile.resolve("$newName.${potentialFile.extension}")
                                newPaths.add(Pair(File(potentialFile.path), newFile))
                                potentialFile = newFile
                            }

                            potentialFile.writeBytes(zip.readBytes())
                        }
                    }

                    entry = zip.nextEntry
                }
            }

            data?.let { d ->
                newPaths.forEach { (old, new) ->
                    val oldInfo = d.find { File(it.path).absolutePath == old.absolutePath } ?: return@forEach
                    d.add(oldInfo.copy(path = new.path))
                    d.remove(oldInfo)
                }

                val newIDs = ids.multipleUniqueIDs(d.size)
                val newData: MutableList<MediaInfo> = mutableListOf()
                d.forEachIndexed { index, it ->
                    newData.add(it.copy(id = newIDs[index]))
                }

                return newData

            } ?: return null
        }
    }
}