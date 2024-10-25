package tag

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.Serializable

@Serializable(with = TagCategorySerializer::class)
data class TagCategory(
    var name: String,
    val tags: SnapshotStateList<String> = mutableStateListOf(),
)