package info.media

import getAudioCover
import getAudioDuration
import getImageBitmap
import getImageDimensions
import getVideoDimensions
import getVideoDuration
import isAudioFileSupported
import isVideoFileSupported
import properties.DataFolder
import uniqueName
import utilities.HasID
import utilities.uniqueId
import java.io.File

class MediaInfoFactory {
    companion object {
        fun makeFromFile(
            file: File,
            ids: List<HasID> = listOf(),
            createCopy: Boolean = true,
        ): MediaInfo? {
            if (!file.exists()) return null

            val type = determineType(file) ?: return null
            val name = createName(file, type, createCopy)
            val newFile = createFile(file, type, createCopy)

            when (type) {
                MediaType.Image -> {
                    val dimensions = getImageDimensions(newFile) ?: return null
                    return ImageInfo(
                        id = ids.uniqueId(),
                        path = newFile.path,
                        name = name,
                        width = dimensions.width,
                        height = dimensions.height,
                    )
                }
                MediaType.GIF -> {
                    val dimensions = getImageDimensions(newFile) ?: return null
                    return GIFInfo(
                        id = ids.uniqueId(),
                        path = newFile.path,
                        name = name,
                        width = dimensions.width,
                        height = dimensions.height,
                    )
                }
                MediaType.Audio -> {
                    val cover = getAudioCover(newFile)
                    return AudioInfo(
                        id = ids.uniqueId(),
                        path = newFile.path,
                        name = name,
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
                        name = name,
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

        private fun determineType(file: File): MediaType? = when {
            file.extension == "gif" -> MediaType.GIF
            getImageBitmap(file) != null -> MediaType.Image
            isVideoFileSupported(file) -> MediaType.Video
            isAudioFileSupported(file) -> MediaType.Audio
            else -> null
        }

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
            return if (createCopy) {
                when (type) {
                    MediaType.Image -> DataFolder.imageFolder.resolve("${file.nameWithoutExtension}.${file.extension}")
                    MediaType.GIF -> DataFolder.gifFolder.resolve("${file.nameWithoutExtension}.${file.extension}")
                    MediaType.Video -> DataFolder.videoFolder.resolve("${file.nameWithoutExtension}.${file.extension}")
                    MediaType.Audio -> DataFolder.audioFolder.resolve("${file.nameWithoutExtension}.${file.extension}")
                }
            } else {
                file

            }.also {
                if (createCopy) {
                    file.copyTo(it)
                }
            }
        }
    }
}