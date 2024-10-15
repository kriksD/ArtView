package properties.data.backup

import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mediaData
import properties.DataFolder
import settings
import utilities.uniqueId

class BackupManager {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val backups = mutableStateListOf<BackupInfo>()

    fun load() {
        if (!DataFolder.backupInfoFile.exists()) return

        backups.clear()

        val infoList = json.decodeFromString<List<BackupInfo>>(DataFolder.backupInfoFile.readText())
        backups.addAll(infoList)
    }

    fun save() {
        DataFolder.backupFolder.mkdirs()
        DataFolder.backupInfoFile.writeText(json.encodeToString(backups.toList()))
    }

    fun createBackup() {
        val id = backups.uniqueId()

        val backupFolder = DataFolder.backupFolder.resolve(id.toString())
        backupFolder.mkdirs()

        DataFolder.mediaFolder.copyRecursively(backupFolder.resolve("media"))
        DataFolder.settingsFile.copyTo(backupFolder.resolve("settings.json"))

        val info = BackupInfo(
            id = backups.uniqueId(),
            date = System.currentTimeMillis(),
            mediaCount = mediaData.mediaList.size,
            spaceUsed = backupFolder.walkTopDown().filter { it.isFile }.sumOf { it.length() },
        )

        backups.add(0, info)

        if (backups.size > settings.backupLimit) {
            removeBackup(backups.last())
        }

        save()
    }

    fun removeBackup(info: BackupInfo) {
        val backupFolder = DataFolder.backupFolder.resolve(info.id.toString())
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
