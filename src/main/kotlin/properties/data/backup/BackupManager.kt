package properties.data.backup

import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.decodeFromString
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
        val info = BackupInfo(
            id = backups.uniqueId(),
            date = System.currentTimeMillis(),
            imageCount = Properties.imagesData().images.size,
        )

        val imagesFolder = File("images")
        val imagesDataFile = File("imagesData.json")
        val settingsFile = File("settings.json")

        val backupFolder = File(folder, info.id.toString())
        backupFolder.mkdirs()

        imagesFolder.copyRecursively(File(backupFolder, "images"))
        imagesDataFile.copyTo(File(backupFolder, "imagesData.json"))
        settingsFile.copyTo(File(backupFolder, "settings.json"))

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
