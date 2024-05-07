package composableFunctions.window

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import calculateWeight
import colorText
import composableFunctions.*
import danbooruClient.DanbooruClient
import danbooruClient.DanbooruPost
import emptyImageBitmap
import normalText
import padding
import properties.Properties
import settings

@Composable
fun AddByURLWindow(
    url: String,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        var name by remember { mutableStateOf(TextFieldValue("")) }
        var description by remember { mutableStateOf(TextFieldValue("")) }
        val selectedTags = remember { mutableStateListOf<String>() }
        val newTags = remember { mutableStateListOf<String>() }
        val newCharacters = remember { mutableStateListOf<String>() }
        val newCopyrights = remember { mutableStateListOf<String>() }
        val newArtists = remember { mutableStateListOf<String>() }
        val selectedNewTags = remember { mutableStateListOf<String>() }

        var danbooruPost by remember { mutableStateOf<DanbooruPost?>(null) }

        LaunchedEffect(true) {
            danbooruPost = DanbooruClient.loadPost(url)

            danbooruPost?.let {
                if (it.tags.isNotEmpty()) {
                    val new = it.tags.filter { tag -> !Properties.imagesData().containsTag(tag) }
                    newTags.addAll(new)
                    selectedNewTags.addAll(new)
                }

                if (it.character.isNotEmpty()) {
                    val new = it.character.filter { tag -> !Properties.imagesData().containsTag(tag) }
                    newCharacters.addAll(new)
                    selectedNewTags.addAll(new)
                }

                if (it.copyright.isNotEmpty()) {
                    val new = it.copyright.filter { tag -> !Properties.imagesData().containsTag(tag) }
                    newCopyrights.addAll(new)
                    selectedNewTags.addAll(new)
                }

                if (it.artist.isNotEmpty()) {
                    val new = it.artist.filter { tag -> !Properties.imagesData().containsTag(tag) }
                    newArtists.addAll(new)
                    selectedNewTags.addAll(new)
                }

                val tagsCombined = it.tags + it.character + it.copyright + it.artist
                val existingTags = tagsCombined.filter { tag -> Properties.imagesData().containsTag(tag) }
                selectedTags.addAll(existingTags)
            }
        }

        LoadingImage(
            danbooruPost?.image ?: emptyImageBitmap,
            name.text,
            modifier = Modifier
                .fillMaxHeight(0.2F)
                .aspectRatio(danbooruPost?.image?.calculateWeight() ?: 1F),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(padding),
        ) {
            Text("Name:", color = colorText, fontSize = normalText)
            BasicTextField(
                name,
                onValueChange = { name = it },
                singleLine = true,
                maxLines = 1,
                textStyle = TextStyle(
                    color = colorText,
                    fontSize = normalText,
                ),
                cursorBrush = SolidColor(colorText),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding),
        ) {
            Text("Description:", color = colorText, fontSize = normalText)
            BasicTextField(
                description,
                onValueChange = { description = it },
                textStyle = TextStyle(
                    color = colorText,
                    fontSize = normalText,
                ),
                cursorBrush = SolidColor(colorText),
                modifier = Modifier.fillMaxWidth()
            )
        }

        val onTagClick: (String) -> Unit = { tag ->
            if (selectedNewTags.contains(tag)) {
                selectedNewTags.remove(tag)
            } else {
                selectedNewTags.add(tag)
                selectedNewTags.sort()
            }
        }

        NewTagsTable(
            name = "New tags:",
            tags = newTags,
            selectedTags = selectedNewTags,
            onTagClick = onTagClick,
        )

        NewTagsTable(
            name = "New characters:",
            tags = newCharacters,
            selectedTags = selectedNewTags,
            onTagClick = onTagClick,
        )

        NewTagsTable(
            name = "New copyrights:",
            tags = newCopyrights,
            selectedTags = selectedNewTags,
            onTagClick = onTagClick,
        )

        NewTagsTable(
            name = "New artists:",
            tags = newArtists,
            selectedTags = selectedNewTags,
            onTagClick = onTagClick,
        )

        Text("Tags:", color = colorText, fontSize = normalText)
        TagTableWithCategories(
            tags = Properties.imagesData().tags,
            selectedTags = selectedTags,
            antiSelectedTags = emptyList(),
            expandable = false,
            onTagClick = {
                if (selectedTags.contains(it)) {
                    selectedTags.remove(it)
                } else {
                    selectedTags.add(it)
                    selectedTags.sort()
                }
            },
            onNew = {
                if (settings.autoSelectCreatedTags)
                    selectedTags.add(it)
            },
            modifier = Modifier.weight(1F),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            ButtonText(
                "Cancel",
                onClick = onCancel
            )

            ButtonText(
                "Save",
                onClick = {
                    danbooruPost?.let {
                        val data = Properties.imagesData()

                        if (newTags.isNotEmpty()) {
                            if (!data.containsTagCategory("Booru Tags")) {
                                data.createCategory("Booru Tags")
                            }

                            data.addAllTags(newTags, "Booru Tags")
                        }

                        if (newCharacters.isNotEmpty()) {
                            if (!data.containsTagCategory("Character")) {
                                data.createCategory("Character")
                            }

                            data.addAllTags(newCharacters, "Character")
                        }

                        if (newCopyrights.isNotEmpty()) {
                            if (!data.containsTagCategory("Copyright")) {
                                data.createCategory("Copyright")
                            }

                            data.addAllTags(newCopyrights, "Copyright")
                        }

                        if (newArtists.isNotEmpty()) {
                            if (!data.containsTagCategory("Artist")) {
                                data.createCategory("Artist")
                            }

                            data.addAllTags(newArtists, "Artist")
                        }

                        val imageInfo = data.addImage(it.image, name.text)
                        imageInfo.tags.addAll(selectedTags + selectedNewTags)
                        imageInfo.name = name.text
                        imageInfo.description = description.text

                        onDone()
                    }
                },
            )
        }
    }
}

@Composable
private fun NewTagsTable(
    name: String,
    tags: List<String>,
    selectedTags: List<String>,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(padding),
        modifier = modifier,
    ) {
        Text(name, color = colorText, fontSize = normalText)
        TagTable(
            tags = tags,
            selectedTags = selectedTags,
            antiSelectedTags = emptyList(),
            expandable = true,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            controls = false,
            clickableTags = true,
            onTagClick = onTagClick,
            modifier = Modifier.weight(1F),
        )
    }
}