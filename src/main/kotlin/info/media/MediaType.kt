package info.media

import getImageBitmap
import isAudioFileSupported
import isVideoFileSupported
import java.io.File

enum class MediaType {
    Image, GIF, Video, Audio;

    companion object {
        fun determineType(file: File): MediaType? = when {
            file.extension == "gif" -> GIF
            getImageBitmap(file) != null -> Image
            isVideoFileSupported(file) -> Video
            isAudioFileSupported(file) -> Audio
            else -> null
        }
    }
}