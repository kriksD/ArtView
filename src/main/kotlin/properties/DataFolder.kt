package properties

import java.io.File

object DataFolder {
    val folder = File("data")

    val mediaFolder = folder.resolve("media")
    val imageFolder = mediaFolder.resolve("image")
    val videoFolder = mediaFolder.resolve("video")
    val gifFolder = mediaFolder.resolve("gif")
    val audioFolder = mediaFolder.resolve("audio")
    val mediaDataFile = mediaFolder.resolve("media_data.json")
    val tagDataFile = mediaFolder.resolve("tag_data.json")

    val filteredFolder = folder.resolve("filtered")

    val settingsFolder = folder.resolve("settings")
    val settingsFile = settingsFolder.resolve("settings.json")

    val styleFolder = folder.resolve("style")
    val styleFile = styleFolder.resolve("style.json")

    val backupFolder = folder.resolve("backups")
    val backupInfoFile = backupFolder.resolve("info.json")

    val cacheFolder = folder.resolve("cache")
    val thumbnailsCacheFolder = cacheFolder.resolve("thumbnails")
}