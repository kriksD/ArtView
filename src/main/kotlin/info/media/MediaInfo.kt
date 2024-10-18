package info.media

import utilities.HasID
import androidx.compose.ui.graphics.ImageBitmap
import openWebpage
import properties.DataFolder
import java.io.File
import java.net.URL

interface MediaInfo : HasID {
    val type: MediaType
    override val id: Int
    val path: String
    var name: String
    var description: String
    var favorite: Boolean
    var hidden: Boolean
    val tags: MutableList<String>
    var source: String?
    var rating: String?

    var thumbnail: ImageBitmap?
    val isThumbnailLoaded: Boolean get() = thumbnail != null

    fun loadThumbnail()

    fun unloadThumbnail() {
        thumbnail = null
    }

    fun thumbnailWeight(): Float

    fun saveFileTo(folder: File) {
        val newFile = File("${folder.path}${File.separator}${path.substringAfterLast("\\").substringAfterLast("/")}")
        if (newFile.exists()) return

        File(path).copyTo(newFile)
    }

    fun saveFileTo(path: String) = saveFileTo(File(path))

    fun openSource() {
        if (source == null) return
        openWebpage(URL(source))
    }

    fun delete() {
        if (File(path).exists()) File(path).delete()

        val cacheFile = File(DataFolder.thumbnailsCacheFolder, "$id.cache")
        if (cacheFile.exists()) cacheFile.delete()
    }

    fun copy(
        id: Int = this.id,
        path: String = this.path,
        name: String = this.name,
        description: String = this.description,
        favorite: Boolean = this.favorite,
        hidden: Boolean = this.hidden,
        tags: MutableList<String> = this.tags,
        source: String? = this.source,
        rating: String? = this.rating,
    ): MediaInfo
}