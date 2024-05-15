package properties.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.*

@Serializable(with = SettingsSerializer::class)
class Settings(
    language: String = "en",
    background: String = "bg1.png",
    selectedTagsByDefault: Collection<String> = emptyList(),
    antiSelectedTagsByDefault: Collection<String> = listOf("NSFW"),
    autoSelectCreatedTags: Boolean = false,
    addTagsToCreatedGroups: Boolean = false,
    showDebug: Boolean = false,
    addTagButtonPosition: TagControlsPosition = TagControlsPosition.Right,
    filterTagButtonPosition: TagControlsPosition = TagControlsPosition.Right,
    booruTagsCategoryName: String = "Booru Tags",
    characterTagsCategoryName: String = "Character",
    copyrightTagsCategoryName: String = "Copyright",
    artistTagsCategoryName: String = "Artist",
    metaTagsCategoryName: String = "Meta",
) {
    var language by mutableStateOf(language)
    var background by mutableStateOf(background)
    val selectedTagsByDefault = mutableStateListOf<String>().also { it.addAll(selectedTagsByDefault) }
    val antiSelectedTagsByDefault = mutableStateListOf<String>().also { it.addAll(antiSelectedTagsByDefault) }
    var autoSelectCreatedTags by mutableStateOf(autoSelectCreatedTags)
    var addTagsToCreatedGroups by mutableStateOf(addTagsToCreatedGroups)
    var showDebug by mutableStateOf(showDebug)
    var addTagButtonPosition by mutableStateOf(addTagButtonPosition)
    var filterTagButtonPosition by mutableStateOf(filterTagButtonPosition)

    var booruTagsCategoryName by mutableStateOf(booruTagsCategoryName)
    var characterTagsCategoryName by mutableStateOf(characterTagsCategoryName)
    var copyrightTagsCategoryName by mutableStateOf(copyrightTagsCategoryName)
    var artistTagsCategoryName by mutableStateOf(artistTagsCategoryName)
    var metaTagsCategoryName by mutableStateOf(metaTagsCategoryName)
}