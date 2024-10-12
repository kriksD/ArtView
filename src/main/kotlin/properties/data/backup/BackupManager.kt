package properties.data.backup

import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import properties.Properties
import settings
import uniqueId
import java.io.File

class BackupManager {
    private val folder = File("data/backup")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val backups = mutableStateListOf<BackupInfo>()

    fun load() {
        val infoFile = File(folder, "info.txt")
        if (!infoFile.exists()) return

        backups.clear()

        val infoList = json.decodeFromString<List<BackupInfo>>(infoFile.readText())
        backups.addAll(infoList)
    }

    fun save() {
        folder.mkdirs()
        val infoFile = File(folder, "info.txt")
        infoFile.writeText(json.encodeToString(backups.toList()))
    }

    fun createBackup() {
        val id = backups.uniqueId()

        val imagesFolder = File("data/images")
        val imagesDataFile = File("data/images_data.json")
        val settingsFile = File("data/settings.json")

        val backupFolder = File(folder, id.toString())
        backupFolder.mkdirs()

        imagesFolder.copyRecursively(File(backupFolder, "images"))
        imagesDataFile.copyTo(File(backupFolder, "images_data.json"))
        settingsFile.copyTo(File(backupFolder, "settings.json"))

        val info = BackupInfo(
            id = backups.uniqueId(),
            date = System.currentTimeMillis(),
            mediaCount = Properties.mediaData().mediaList.size,
            spaceUsed = backupFolder.walkTopDown().filter { it.isFile }.sumOf { it.length() },
        )

        backups.add(0, info)

        if (backups.size > settings.backupLimit) {
            removeBackup(backups.last())
        }

        save()
    }

    fun removeBackup(info: BackupInfo) {
        val backupFolder = File(folder, info.id.toString())
        backupFolder.deleteRecursively()
        backups.remove(info)
        save()
    }

    fun limitUpdated(limit: Int) {
        while (backups.size > limit) {
            removeBackup(backups.last())
        }

        save()
    }
}
