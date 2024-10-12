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
import colorTextError
import composableFunctions.*
import booruClient.clients.BooruClient
import booruClient.BooruPost
import composableFunctions.views.ButtonText
import composableFunctions.views.LoadingImage
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
        val newMeta = remember { mutableStateListOf<String>() }
        val selectedNewTags = remember { mutableStateListOf<String>() }

        var booruPost by remember { mutableStateOf<BooruPost?>(null) }
        var failedToLoad by remember { mutableStateOf(false) }

        LaunchedEffect(true) {
            booruPost = BooruClient.loadPost(url)
            if (booruPost == null) {
                failedToLoad = true
            }

            booruPost?.let { post ->
                post.title?.let { name = TextFieldValue(post.title) }

                post.rating?.let {
                    if (
                        !selectedTags.contains("NSFW")
                        && Properties.mediaData().containsTag("NSFW")
                        && it.lowercase() != "g"
                        && it.lowercase() != "general"
                    ) {
                        selectedTags.add("NSFW")
                    }
                }

                if (post.tags.isNotEmpty()) {
                    val new = post.tags.filter { tag -> !Properties.mediaData().containsTag(tag) }
                    newTags.addAll(new)
                    selectedNewTags.addAll(new)
                }

                if (post.character.isNotEmpty()) {
                    val new = post.character.filter { tag -> !Properties.mediaData().containsTag(tag) }
                    newCharacters.addAll(new)
                    selectedNewTags.addAll(new)
                }

                if (post.copyright.isNotEmpty()) {
                    val new = post.copyright.filter { tag -> !Properties.mediaData().containsTag(tag) }
                    newCopyrights.addAll(new)
                    selectedNewTags.addAll(new)
                }

                if (post.artist.isNotEmpty()) {
                    val new = post.artist.filter { tag -> !Properties.mediaData().containsTag(tag) }
                    newArtists.addAll(new)
                    selectedNewTags.addAll(new)
                }

                if (post.meta.isNotEmpty()) {
                    val new = post.meta.filter { tag -> !Properties.mediaData().containsTag(tag) }
                    newMeta.addAll(new)
                    selectedNewTags.addAll(new)
                }

                val tagsCombined = post.tags + post.character + post.copyright + post.artist + post.meta
                val existingTags = tagsCombined.filter { tag -> Properties.mediaData().containsTag(tag) }
                selectedTags.addAll(existingTags)
            }
        }

        Row {
            LoadingImage(
                if (failedToLoad) emptyImageBitmap else booruPost?.image,
                name.text,
                modifier = Modifier
                    .fillMaxHeight(0.2F)
                    .aspectRatio(booruPost?.image?.calculateWeight() ?: 1F),
            )

            if (failedToLoad) {
                Text("Failed to load :(", color = colorTextError, fontSize = normalText)
            }
        }

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
                modifier = Modifier.weight(1F),
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
                modifier = Modifier.weight(1F),
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

        NewTagsGrid(
            name = "New tags:",
            tags = newTags,
            selectedTags = selectedNewTags,
            onTagClick = onTagClick,
        )

        NewTagsGrid(
            name = "New characters:",
            tags = newCharacters,
            selectedTags = selectedNewTags,
            onTagClick = onTagClick,
        )

        NewTagsGrid(
            name = "New copyrights:",
            tags = newCopyrights,
            selectedTags = selectedNewTags,
            onTagClick = onTagClick,
        )

        NewTagsGrid(
            name = "New artists:",
            tags = newArtists,
            selectedTags = selectedNewTags,
            onTagClick = onTagClick,
        )

        NewTagsGrid(
            name = "New meta:",
            tags = newMeta,
            selectedTags = selectedNewTags,
            onTagClick = onTagClick,
        )

        Text("Tags:", color = colorText, fontSize = normalText)
        TagGridWithCategories(
            tags = Properties.mediaData().tags,
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
                enabled = booruPost != null,
                onClick = {
                    booruPost?.let { post ->
                        val data = Properties.mediaData()

                        data.addNewTags(newTags, selectedNewTags, settings.booruTagsCategoryName)
                        data.addNewTags(newCharacters, selectedNewTags, settings.characterTagsCategoryName)
                        data.addNewTags(newCopyrights, selectedNewTags, settings.copyrightTagsCategoryName)
                        data.addNewTags(newArtists, selectedNewTags, settings.artistTagsCategoryName)
                        data.addNewTags(newMeta, selectedNewTags, settings.metaTagsCategoryName)

                        val mediaInfo = data.addMedia(post.image, name.text)
                        mediaInfo.tags.addAll(selectedTags + selectedNewTags)
                        mediaInfo.name = name.text
                        mediaInfo.description = description.text
                        post.source?.let { mediaInfo.source = it }
                        post.rating?.let { mediaInfo.rating = it }

                        onDone()
                    }
                },
            )
        }
    }
}

@Composable
private fun NewTagsGrid(
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
        TagGrid(
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