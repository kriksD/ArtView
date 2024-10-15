package info

import androidx.compose.ui.graphics.ImageBitmap
import getImageBitmap
import properties.DataFolder
import saveWebPTo
import java.io.File

class CacheManager {
    fun cacheThumbnail(image: ImageBitmap, id: Int) {
        val folder = DataFolder.thumbnailsCacheFolder
        folder.mkdirs()
        image.saveWebPTo(File(folder, "$id.cache"))
    }

    fun loadThumbnail(id: Int): ImageBitmap? {
        val file = File(DataFolder.thumbnailsCacheFolder, "$id.cache")
        if (!file.exists()) return null
        return getImageBitmap(file)
    }
}