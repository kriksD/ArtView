package info.media

import getAudioDuration
import getImageDimensions
import getVideoDimensions
import getVideoDuration
import java.awt.Dimension
import java.io.File

class MediaInfoFixer {
    companion object {
        fun isMediaInfoBroken(info: MediaInfo): Boolean {
            val file = File(info.path)
            if (!file.exists()) return true

            return when (info) {
                is ImageInfo -> isImageInfoBroken(info)
                is GIFInfo -> isGIFInfoBroken(info)
                is VideoInfo -> isVideoInfoBroken(info)
                is AudioInfo -> isAudioInfoBroken(info)
                else -> false
            }
        }

        private fun isImageInfoBroken(info: ImageInfo): Boolean {
            return info.height <= -1 || info.width <= -1
        }

        private fun isGIFInfoBroken(info: GIFInfo): Boolean {
            return info.height <= -1 || info.width <= -1
        }

        private fun isVideoInfoBroken(info: VideoInfo): Boolean {
            return info.height <= -1 || info.width <= -1 || info.duration <= -1L
        }

        private fun isAudioInfoBroken(info: AudioInfo): Boolean {
            return info.duration <= -1L

        }

        fun fixMediaInfo(info: MediaInfo): MediaInfo? {
            val file = File(info.path)
            if (!file.exists()) {
                return if (info.path.contains("[Error: File not found]")) {
                    info
                } else {
                    info.copy(path = "[Error: File not found] ${info.path}",)
                }
            }

            return when (info) {
                is ImageInfo -> fixImageInfo(info)
                is GIFInfo -> fixGIFInfo(info)
                is VideoInfo -> fixVideoInfo(info)
                is AudioInfo -> fixAudioInfo(info)
                else -> null
            }
        }

        private fun fixImageInfo(info: ImageInfo): ImageInfo? {
            if (info.height != -1 && info.width != -1) return info

            val file = File(info.path)
            if (!file.exists()) return null

            val dimensions = getImageDimensions(file) ?: return info

            return info.copy(
                width = dimensions.width,
                height = dimensions.height,
            )
        }

        private fun fixGIFInfo(info: GIFInfo): GIFInfo? {
            if (info.height != -1 && info.width != -1) return info

            val file = File(info.path)
            if (!file.exists()) return null

            val dimensions = getImageDimensions(file) ?: return info

            return info.copy(
                width = dimensions.width,
                height = dimensions.height,
            )
        }

        private fun fixVideoInfo(info: VideoInfo): VideoInfo? {
            if (info.height != -1 && info.width != -1 && info.duration != -1L) return info

            val file = File(info.path)
            if (!file.exists()) return null

            val dimensions = if (info.height != -1 && info.width != -1) {
                Dimension(info.width, info.height)
            } else {
                getVideoDimensions(file) ?: Dimension(-1, -1)
            }

            val duration = if (info.duration != -1L) {
                info.duration
            } else {
                getVideoDuration(file) ?: -1L
            }

            return info.copy(
                width = dimensions.width,
                height = dimensions.height,
                duration = duration,
            )
        }

        private fun fixAudioInfo(info: AudioInfo): AudioInfo? {
            if (info.duration != -1L) return info

            val file = File(info.path)
            if (!file.exists()) return null

            val duration = getAudioDuration(file) ?: return info

            return info.copy(
                duration = duration,
            )
        }
    }
}