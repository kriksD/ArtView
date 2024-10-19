package properties.data.backup

import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mediaData
import properties.DataFolder
import settings
import utilities.uniqueId
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupManager {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
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

        val backupZip = DataFolder.backupFolder.resolve("$id.zip")
        DataFolder.backupFolder.mkdirs()

        ZipOutputStream(FileOutputStream(backupZip)).use { zip ->
            DataFolder.mediaFolder.walkTopDown()
                .filter { it.isFile }
                .forEach { file ->
                    zip.putNextEntry(ZipEntry(file.relativeTo(DataFolder.folder).path))
                    file.inputStream().use { input ->
                        input.copyTo(zip)
                    }
                    zip.closeEntry()
                }

            zip.putNextEntry(ZipEntry(DataFolder.settingsFile.relativeTo(DataFolder.folder).path))
            DataFolder.settingsFile.inputStream().use { input ->
                input.copyTo(zip)
            }
            zip.closeEntry()
        }

        val info = BackupInfo(
            id = backups.uniqueId(),
            date = System.currentTimeMillis(),
            mediaCount = mediaData.mediaList.size,
            spaceUsed = backupZip.length(),
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
