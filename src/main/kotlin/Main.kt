// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import properties.Properties
import java.io.File
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    Properties.loadStyle()
    Properties.loadSettings()
    Properties.loadLanguage(settings.language)
    Properties.loadData()

    val images = remember { Properties.imagesData().images.toState() }
    var filteredImages by remember { mutableStateOf(images.toList()) }
    var selectedImage by remember { mutableStateOf<ImageInfo?>(null) }

    val tags = remember { Properties.imagesData().tags.toState() }
    val selectedTags = remember { mutableStateListOf<String>() }
    val antiSelectedTags = remember { mutableStateListOf<String>() }

    var isFirstTime by remember { mutableStateOf(true) }
    if (isFirstTime) {
        isFirstTime = false

        File("images").mkdir()
        antiSelectedTags.add("NSFW")
        filteredImages = images.filter { image -> antiSelectedTags.none { tag -> image.tags.contains(tag) } }
    }

    val windowState = rememberWindowState(
        width = 1400.dp,
        height = 700.dp,
        position = WindowPosition.Aligned(Alignment.Center)
    )

    Window(onCloseRequest = ::exitApplication, state = windowState) {
        var menuItem by remember { mutableStateOf(MenuItem.Images) }
        var isDroppable by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .background(colorBackgroundLighter)
                .onExternalDrag(
                    onDragStart = { externalDragValue ->
                        isDroppable = externalDragValue.dragData is DragData.FilesList
                    },
                    onDragExit = {
                        isDroppable = false
                    },
                    onDrop = { externalDragValue ->
                        isDroppable = false
                        when (val dragData = externalDragValue.dragData) {
                            is DragData.FilesList -> {
                                val paths = dragData.readFiles()
                                paths.forEach { path ->
                                    addImage(URI(path).path)?.let { images.add(0, it) }
                                }
                            }
                        }
                        Properties.saveData()
                    }
                ),
        ) {
            LeftSideMenu(
                menuItem,
                onOptionSelected = { menuItem = it },
                modifier = Modifier.fillMaxHeight(),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(
                    menuItem,
                    animationSpec = tween(normalAnimationDuration),
                    modifier = Modifier.fillMaxSize(),
                ) { item ->

                    @Composable
                    fun ImageTableView(
                        filter: (ImageInfo) -> Boolean,
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        Column {
                            TagTable(
                                tags = tags,
                                selectedTags = selectedTags,
                                antiSelectedTags = antiSelectedTags,
                                onTagClick = {
                                    if (selectedTags.contains(it)) {
                                        selectedTags.remove(it)
                                        antiSelectedTags.add(it)

                                    } else if (antiSelectedTags.contains(it)) {
                                        selectedTags.remove(it)
                                        antiSelectedTags.remove(it)

                                    } else {
                                        selectedTags.add(it)
                                        antiSelectedTags.remove(it)
                                    }
                                },
                                onNew = {
                                    if (it.isNotEmpty() && !selectedTags.contains(it)) {
                                        tags.add(it)
                                        tags.sort()
                                        Properties.imagesData().tags.add(it)
                                        Properties.imagesData().tags.sort()
                                        Properties.saveData()
                                    }
                                },
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorBackgroundLighter),
                            )

                            ImageGrid(
                                images.filter(filter).also { filteredImages = it },
                                onOpen = { selectedImage = it },
                                modifier = Modifier.weight(1F),
                            )
                        }
                    }

                    when (item) {
                        MenuItem.Images -> {
                            ImageTableView(filter = { image ->
                                val hasSelectedTags = selectedTags.all { tag -> image.tags.contains(tag) }
                                val hasAntiSelectedTags = antiSelectedTags.none { tag -> image.tags.contains(tag) }
                                hasSelectedTags && hasAntiSelectedTags
                            })
                        }
                        MenuItem.Favorites -> {
                            ImageTableView(filter = { image ->
                                val hasSelectedTags = selectedTags.all { tag -> image.tags.contains(tag) }
                                val hasAntiSelectedTags = antiSelectedTags.none { tag -> image.tags.contains(tag) }
                                hasSelectedTags && hasAntiSelectedTags && image.favorite
                            })
                        }
                        MenuItem.Collections -> {}
                        MenuItem.Settings -> {}
                    }
                }

                var isEditing by remember { mutableStateOf(false) }
                ImagePreview(
                    selectedImage,
                    onClose = {
                        selectedImage = null
                        isEditing = false
                    },
                    onNext = { filteredImages.getOrNull(filteredImages.indexOf(selectedImage) + 1)?.let { selectedImage = it } },
                    onPrevious = { filteredImages.getOrNull(filteredImages.indexOf(selectedImage) - 1)?.let { selectedImage = it } },
                    onDelete = {
                        selectedImage?.let { Properties.imagesData().delete(it) }
                        images.remove(selectedImage)
                        selectedImage = null
                        isEditing = false
                    },
                    onEdit = { isEditing = true },
                    modifier = Modifier.fillMaxSize(),
                )

                AppearDisappearAnimation(
                    isEditing,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.65F)
                            .heightIn(0.dp, (window.height * 0.8F).dp)
                            .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                            .padding(padding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        selectedImage?.let { imgInfo ->
                            EditImageWindow(
                                tags = tags,
                                imageInfo = imgInfo,
                                onCancel = { isEditing = false },
                                onFinish = { newImageInfo ->
                                    val index = images.indexOf(images.find { it.path == newImageInfo.path })
                                    images[index] = newImageInfo
                                    Properties.imagesData().images[index] = newImageInfo
                                    Properties.saveData()
                                    selectedImage = newImageInfo
                                    isEditing = false
                                },
                                onNewTag = {
                                    if (it.isNotEmpty() && !selectedTags.contains(it)) {
                                        tags.add(it)
                                        tags.sort()
                                        Properties.imagesData().tags.add(it)
                                        Properties.imagesData().tags.sort()
                                        Properties.saveData()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}