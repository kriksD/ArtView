package properties.data.backup

import HasID
import kotlinx.serialization.Serializable

@Serializable
data class BackupInfo(
    override val id: Int,
    val date: Long,
    val mediaCount: Int,
    val spaceUsed: Long,
): HasID {
    val folderPath = "data/backup/$id"
}