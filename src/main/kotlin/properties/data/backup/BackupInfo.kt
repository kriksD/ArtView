package properties.data.backup

import HasID
import kotlinx.serialization.Serializable

@Serializable
data class BackupInfo(
    override val id: Int,
    val date: Long,
    val imageCount: Int,
): HasID {
    val folderPath = "data/backup/$id"
}