// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import composableFunctions.*
import properties.Properties
import java.io.File
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    var isFirstTime1 by remember { mutableStateOf(true) }
    if (isFirstTime1) {
        isFirstTime1 = false

        Properties.loadStyle()
        Properties.loadSettings()
        Properties.loadLanguage(settings.language)
        Properties.loadData()
    }

    val imageStorage by remember { mutableStateOf(ImageStorage()) }
    var imageLoader by remember { mutableStateOf(ImageLoader()) }

    val selectedTags = remember { mutableStateListOf<String>() }
    val antiSelectedTags = remember { mutableStateListOf<String>() }

    var isEditingGroup by remember { mutableStateOf(false) }

    var isFirstTime by remember { mutableStateOf(true) }
    if (isFirstTime) {
        isFirstTime = false

        File("images").mkdir()
        antiSelectedTags.addAll(settings.anti_selected_tags_by_default)
        selectedTags.addAll(settings.selected_tags_by_default)

        imageStorage.filter(FilterBuilder().tags(selectedTags).antiTags(antiSelectedTags))
    }

    LaunchedEffect(imageLoader) {
        imageLoader.load()
    }

    val windowState = rememberWindowState(
        width = 1400.dp,
        height = 700.dp,
        position = WindowPosition.Aligned(Alignment.Center)
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Art View ${Properties.version} 🎨🎀💝💖",
    ) {
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
                                    Properties.imagesData().addImage(URI(path).path)
                                    imageStorage.update()
                                }
                            }
                        }
                        Properties.saveData()
                    }
                ),
        ) {
            LeftSideMenu(
                menuItem,
                onOptionSelected = {
                    menuItem = it
                    when (it) {
                        MenuItem.Images -> {
                            imageStorage.setFilter(FilterBuilder().tags(selectedTags).antiTags(antiSelectedTags))
                            imageStorage.withGroups = false
                            imageStorage.update()
                            imageLoader.cancel()
                            imageLoader = ImageLoader()
                        }

                        MenuItem.Favorites -> {
                            imageStorage.setFilter(FilterBuilder().tags(selectedTags).antiTags(antiSelectedTags).favorite())
                            imageStorage.withGroups = false
                            imageStorage.update()
                            imageLoader.cancel()
                            imageLoader = ImageLoader()
                        }

                        MenuItem.Groups -> {
                            imageStorage.setFilter(FilterBuilder().tags(selectedTags).antiTags(antiSelectedTags))
                            imageStorage.withGroups = true
                            imageStorage.update()
                            imageLoader.cancel()
                            imageLoader = ImageLoader()
                        }

                        MenuItem.Settings -> {
                            imageStorage.reset()
                            imageLoader.cancel()
                            imageLoader = ImageLoader()
                        }
                    }
                },
                modifier = Modifier.fillMaxHeight(),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(
                    menuItem,
                    animationSpec = tween(normalAnimationDuration),
                    modifier = Modifier.fillMaxSize(),
                ) { item ->

                    @Composable
                    fun ImageTableView() {
                        var expanded by remember { mutableStateOf(false) }
                        Column {
                            TagTableWithCategories(
                                tags = Properties.imagesData().tags,
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

                                    imageStorage.updateFilterTags(selectedTags, antiSelectedTags)
                                    imageStorage.update()
                                    imageLoader.cancel()
                                    imageLoader = ImageLoader()
                                },
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorBackgroundLighter),
                            )

                            ImageGridWithLoading(
                                images = imageStorage.filteredImages,
                                imageLoader = imageLoader,
                                checkedList = imageStorage.selectedImages,
                                onCheckedClick = { imgInfo, isSelected ->
                                    if (isSelected) {
                                        imageStorage.select(imgInfo)
                                    } else {
                                        imageStorage.deselect(imgInfo)
                                    }
                                },
                                onOpen = {
                                    if (imageStorage.selectedImages.isEmpty()) {
                                        imageStorage.open(it)
                                    } else {
                                        if (!imageStorage.selectedImages.contains(it)) {
                                            imageStorage.select(it)
                                        } else {
                                            imageStorage.deselect(it)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1F),
                            )
                        }
                    }

                    when (item) {
                        MenuItem.Images -> {
                            ImageTableView()
                        }
                        MenuItem.Favorites -> {
                            ImageTableView()
                        }
                        MenuItem.Groups -> {
                            if (imageStorage.openedImageGroup == null) {
                                var expanded by remember { mutableStateOf(false) }
                                Column {
                                    TagTableWithCategories(
                                        tags = Properties.imagesData().tags,
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

                                            imageStorage.updateFilterTags(selectedTags, antiSelectedTags)
                                            imageStorage.update()
                                            imageLoader.cancel()
                                            imageLoader = ImageLoader()
                                        },
                                        expanded = expanded,
                                        onExpandedChange = { expanded = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(colorBackgroundLighter),
                                    )
                                    ImageGroupGrid(
                                        imageGroups = imageStorage.filteredGroups,
                                        imageLoader = imageLoader,
                                        checkedList = imageStorage.selectedGroups,
                                        onCheckedClick = { imgGroup, isSelected ->
                                            if (isSelected) {
                                                imageStorage.select(imgGroup)
                                            } else {
                                                imageStorage.deselect(imgGroup)
                                            }
                                        },
                                        onOpen = {
                                            if (imageStorage.selectedGroups.isEmpty()) {
                                                imageStorage.openGroup(it)
                                            } else {
                                                if (!imageStorage.selectedGroups.contains(it)) {
                                                    imageStorage.select(it)
                                                } else {
                                                    imageStorage.deselect(it)
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1F),
                                    )
                                }
                            } else {
                                ImageGroupPreview(
                                    imageStorage.openedImageGroup!!,
                                    imageLoader = imageLoader,
                                    onClose = { imageStorage.closeGroup() },
                                    onEdit = { isEditingGroup = true },
                                    onSelectedUpdated = { newSelected ->
                                        newSelected.forEach { imageStorage.select(it) }
                                    },
                                    onImageSelected = { img, _ ->
                                        imageStorage.open(img)
                                    }
                                )
                            }
                        }
                        MenuItem.Settings -> { SettingsScreen(modifier = Modifier.fillMaxWidth()) }
                    }
                }

                var isEditing by remember { mutableStateOf(false) }
                ImagePreview(
                    imageStorage.openedImage,
                    onClose = {
                        imageStorage.close()
                        isEditing = false
                    },
                    onNext = { imageStorage.next() },
                    onPrevious = { imageStorage.previous() },
                    onDelete = {
                        imageStorage.delete(listOfNotNull(imageStorage.openedImage))
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
                    imageStorage.openedImage?.let { imgInfo ->
                        EditImageWindow(
                            imageInfo = imgInfo,
                            onCancel = { isEditing = false },
                            onDone = { newImageInfo ->
                                val index = Properties.imagesData().images.indexOf(Properties.imagesData().images.find { it.path == newImageInfo.path })
                                Properties.imagesData().images[index] = newImageInfo
                                Properties.saveData()
                                isEditing = false
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.65F)
                                .heightIn(0.dp, (window.height * 0.8F).dp)
                                .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                                .padding(padding),
                        )
                    }
                }

                AppearDisappearAnimation(
                    isEditingGroup,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    imageStorage.openedImageGroup?.let { imgGroup ->
                        EditImageGroupWindow(
                            imageGroup = imgGroup,
                            onCancel = { isEditingGroup = false },
                            onDone = { newImageGroup ->
                                imageStorage.openedImageGroup?.name = newImageGroup.name
                                imageStorage.openedImageGroup?.description = newImageGroup.description
                                imageStorage.openedImageGroup?.tags?.clear()
                                imageStorage.openedImageGroup?.tags?.addAll(newImageGroup.tags)
                                Properties.saveData()

                                isEditingGroup = false
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.65F)
                                .heightIn(0.dp, (window.height * 0.8F).dp)
                                .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                                .padding(padding),
                        )
                    }
                }

                var isEditingTags by remember { mutableStateOf(false) }
                var isAddingImagesToGroup by remember { mutableStateOf(false) }
                AppearDisappearAnimation(
                    imageStorage.selectedImages.isNotEmpty() || imageStorage.selectedGroups.isNotEmpty(),
                    normalAnimationDuration,
                    Modifier
                        .padding(biggerPadding)
                        .align(Alignment.BottomStart),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(padding),
                    ) {
                        ButtonText(
                            "Select all",
                            onClick = {
                                if (imageStorage.selectedImages.isNotEmpty()) {
                                    imageStorage.selectAll()

                                } else if (imageStorage.selectedGroups.isNotEmpty()) {
                                    imageStorage.selectAllGroups()
                                }
                            },
                        )
                        ButtonText(
                            "Deselect all",
                            onClick = {
                                imageStorage.deselectAll()
                                imageStorage.deselectAllGroups()
                            },
                        )
                        ButtonText(
                            "Delete selected images/groups",
                            onClick = {
                                /*if (selectedImages.isNotEmpty()) {
                                    selectedImages.forEach { it.delete() }
                                    Properties.imagesData().images.removeAll(selectedImages)
                                    selectedImages.clear()
                                    Properties.saveData()
                                    imageLoader.update()

                                } else if (selectedImageGroups.isNotEmpty()) {
                                    groups.removeAll(selectedImageGroups)
                                    Properties.imagesData().imageGroups.removeAll(selectedImageGroups)
                                    selectedImageGroups.clear()
                                    Properties.saveData()
                                    imageLoader.update()
                                }*/
                            },
                        )
                        ButtonText(
                            "Save selected images in new folder",
                            onClick = { imageStorage.saveImageFilesTo() },
                        )
                        ButtonText(
                            "Manage tags for all",
                            onClick = { isEditingTags = true },
                        )
                        if (imageStorage.selectedImages.isNotEmpty()) {
                            ButtonText(
                                "Create group",
                                onClick = { imageStorage.createNewGroup() },
                            )
                            ButtonText(
                                "Add to group",
                                onClick = { isAddingImagesToGroup = true },
                            )
                        }
                    }
                }

                AppearDisappearAnimation(
                    isEditingTags,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    val newTags = remember { mutableStateListOf<String>() }
                    val removeTags = remember { mutableStateListOf<String>() }

                    EditImageTagsWindow(
                        newTags = newTags,
                        removeTags = removeTags,
                        onTagClick = {
                            if (newTags.contains(it)) {
                                newTags.remove(it)
                                removeTags.add(it)

                            } else if (removeTags.contains(it)) {
                                newTags.remove(it)
                                removeTags.remove(it)

                            } else {
                                newTags.add(it)
                                removeTags.remove(it)
                            }
                        },
                        onNew = {
                            if (settings.auto_select_created_tags)
                                newTags.add(it)
                        },
                        onDone = {
                            if (imageStorage.selectedImages.isNotEmpty()) {
                                imageStorage.selectedImages.forEach { ii ->
                                    ii.tags.addAll(newTags.filter { !ii.tags.contains(it) })
                                    ii.tags.removeAll(removeTags)
                                }

                            } else if (imageStorage.selectedGroups.isNotEmpty()) {
                                imageStorage.selectedGroups.forEach { ig ->
                                    ig.tags.addAll(newTags.filter { !ig.tags.contains(it) })
                                    ig.tags.removeAll(removeTags)
                                }
                            }

                            Properties.saveData()
                            imageStorage.update()
                            newTags.clear()
                            removeTags.clear()

                            isEditingTags = false
                        },
                        onCancel = {
                            newTags.clear()
                            removeTags.clear()

                            isEditingTags = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.65F)
                            .heightIn(0.dp, (window.height * 0.8F).dp)
                            .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                            .padding(padding),
                    )
                }

                AppearDisappearAnimation(
                    isAddingImagesToGroup,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    ImageGroupListWindow(
                        onDone = { imageGroup ->
                            imageGroup.imagePaths.addAll(imageStorage.selectedImages.map { it.path }.filter { !imageGroup.imagePaths.contains(it) })

                            Properties.saveData()
                            imageStorage.update()

                            isAddingImagesToGroup = false
                        },
                        onCancel = {
                            isAddingImagesToGroup = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.65F)
                            .heightIn(0.dp, (window.height * 0.8F).dp)
                            .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                            .padding(padding),
                    )
                }

                if (true) {
                    Column(
                        modifier = Modifier
                            .background(colorBackground.copy(transparencySecond))
                            .align(Alignment.BottomEnd)
                            .padding(padding),
                    ) {
                        Text(
                            "Loaded images: ${Properties.imagesData().images.size}/${Properties.imagesData().images.count { it.isLoaded }}",
                            color = colorText,
                            fontSize = normalText,
                        )
                        Text(
                            "Filtered images: ${Properties.imagesData().images.size}/${imageStorage.filteredImages.size}",
                            color = colorText,
                            fontSize = normalText,
                        )
                        Text(
                            "Selected images: ${imageStorage.filteredImages.size}/${imageStorage.selectedImages.size}",
                            color = colorText,
                            fontSize = normalText,
                        )
                        Text(
                            "Filtered groups: ${Properties.imagesData().imageGroups.size}/${imageStorage.filteredGroups.size}",
                            color = colorText,
                            fontSize = normalText,
                        )
                        Text(
                            "Selected groups: ${imageStorage.filteredGroups.size}/${imageStorage.selectedGroups.size}",
                            color = colorText,
                            fontSize = normalText,
                        )
                        Text(
                            "Is loading: ${imageLoader.isLoading}",
                            color = colorText,
                            fontSize = normalText,
                        )
                        Text(
                            "Loading requests: ${imageLoader.requestAmount}",
                            color = colorText,
                            fontSize = normalText,
                        )
                    }
                }
            }
        }
    }
}