package properties.data.backup

import utilities.HasID
import kotlinx.serialization.Serializable
import properties.DataFolder

@Serializable
data class BackupInfo(
    override val id: Int,
    val date: Long,
    val mediaCount: Int,
    val spaceUsed: Long,
): HasID {
    val folderPath: String = DataFolder.backupFolder.resolve("$id").path
}