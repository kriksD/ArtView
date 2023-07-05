package composableFunctions

import TagCategory
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import colorBackground
import colorBackgroundDelete
import colorBackgroundSecondLighter
import colorText
import colorTextSuccess
import iconSize
import normalAnimationDuration
import normalText
import padding
import properties.Properties
import smallCorners

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TagTableWithCategories(
    tags: List<TagCategory>,
    selectedTags: List<String> = emptyList(),
    antiSelectedTags: List<String> = emptyList(),
    controls: Boolean = true,
    expandable: Boolean = true,
    expandableCategories: Boolean = true,
    expanded: Boolean = true,
    onExpandedChange: (Boolean) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (expanded) {
        var mousePosition by remember { mutableStateOf(Offset.Zero) }
        Box(
            modifier = modifier
                .onPointerEvent(PointerEventType.Move) {
                    mousePosition = it.changes.first().position
                }
        ) {
            var draggingTag by remember { mutableStateOf<String?>(null) }
            var isDraggingTag by remember { mutableStateOf(false) }

            Column(modifier = modifier.verticalScroll(rememberScrollState())) {
                tags.forEach { category ->
                    var categoryExpanded by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .onPointerEvent(PointerEventType.Move) {
                                if (!isDraggingTag) {
                                    draggingTag?.let { tag ->
                                        Properties.imagesData().moveTag(tag, category.name)
                                        Properties.saveData()
                                        draggingTag = null
                                    }
                                }
                            }
                    ) {
                        Text("${category.name}:", color = colorText, fontSize = normalText)
                        TagTable(
                            category.tags,
                            selectedTags,
                            antiSelectedTags,
                            controls = controls,
                            scrollable = false,
                            expandable = expandableCategories,
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it },
                            onTagClick = onTagClick,
                            onNew = { tag ->
                                if (tag.isNotEmpty()) {
                                    Properties.imagesData().addTag(tag, category.name)
                                    Properties.saveData()
                                }
                            },
                            onTagDrag = { tag ->
                                isDraggingTag = true
                                draggingTag = tag
                            },
                            onTagDrop = {
                                isDraggingTag = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            if (draggingTag != null) {
                Tag(draggingTag ?: "ERROR", false, modifier = Modifier.offset(mousePosition.x.dp, mousePosition.y.dp))
            }

            if (expandable) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = colorText,
                    modifier = Modifier
                        .size(iconSize)
                        .align(Alignment.TopEnd)
                        .clickable { onExpandedChange(false) }
                )
            }
        }
    } else {
        Row(
            modifier = modifier,
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .weight(1F),
            ) {
                AllTags(
                    tags = tags.find { it.name == "Other" }?.tags ?: emptyList(),
                    selectedTags = selectedTags,
                    antiSelectedTags = antiSelectedTags,
                    controls = controls,
                    onTagClick = { tag -> onTagClick(tag) },
                    onNew = { tag ->
                        Properties.imagesData().addTag(tag)
                        Properties.saveData()
                    },
                )
            }

            if (expandable) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colorText,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onExpandedChange(true) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagTable(
    tags: List<String>,
    selectedTags: List<String> = emptyList(),
    antiSelectedTags: List<String> = emptyList(),
    controls: Boolean = true,
    draggableTags: Boolean = true,
    scrollable: Boolean = true,
    expandable: Boolean = true,
    expanded: Boolean = true,
    onExpandedChange: (Boolean) -> Unit = {},
    onTagDrag: (String) -> Unit = {},
    onTagDrop: (String) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onNew: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (expanded) {
        Box(
            modifier = modifier
        ) {
            FlowRow(modifier = if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier) {
                AllTags(
                    tags = tags,
                    selectedTags = selectedTags,
                    antiSelectedTags = antiSelectedTags,
                    controls = controls,
                    draggableTags = draggableTags,
                    onTagDrag = onTagDrag,
                    onTagDrop = onTagDrop,
                    onTagClick = { tag -> onTagClick(tag) },
                    onNew = onNew,
                )
            }

            if (expandable) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = colorText,
                    modifier = Modifier
                        .size(iconSize)
                        .align(Alignment.TopEnd)
                        .clickable { onExpandedChange(false) }
                )
            }
        }
    } else {
        Row(
            modifier = modifier,
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .weight(1F),
            ) {
                AllTags(
                    tags = tags,
                    selectedTags = selectedTags,
                    antiSelectedTags = antiSelectedTags,
                    controls = controls,
                    draggableTags = draggableTags,
                    onTagDrag = onTagDrag,
                    onTagDrop = onTagDrop,
                    onTagClick = { tag -> onTagClick(tag) },
                    onNew = onNew,
                )
            }

            if (expandable) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colorText,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onExpandedChange(true) }
                )
            }
        }
    }
}

@Composable
private fun AllTags(
    tags: List<String>,
    selectedTags: List<String>,
    antiSelectedTags: List<String>,
    controls: Boolean = true,
    draggableTags: Boolean = true,
    onTagDrag: (String) -> Unit = {},
    onTagDrop: (String) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onNew: (String) -> Unit = {},
) {
    var newTagText by remember { mutableStateOf<TextFieldValue?>(null) }
    var tagFilter by remember { mutableStateOf<TextFieldValue?>(null) }

    tags.filter { it.contains(tagFilter?.text.orEmpty(), true) }.forEach { tag ->
        Tag(
            text = tag,
            isSelected = tag in selectedTags,
            isAntiSelected = tag in antiSelectedTags,
            clickable = controls,
            onClick = { onTagClick(tag) },
            draggableTags = draggableTags,
            onTagDrag = onTagDrag,
            onTagDrop = onTagDrop,
        )
    }

    if (controls) {
        AppearDisappearAnimation(
            newTagText != null,
            normalAnimationDuration,
        ) {
            TagField(
                newTagText ?: TextFieldValue(),
                onValueChange = { newTagText = it },
                onFinish = {
                    onNew(newTagText?.text ?: "")
                    newTagText = null
                },
            )
        }

        AppearDisappearAnimation(
            tagFilter != null,
            normalAnimationDuration,
        ) {
            TagField(
                tagFilter ?: TextFieldValue(),
                onValueChange = { tagFilter = it },
                onFinish = { tagFilter = null },
                icon = Icons.Default.Close,
            )
        }

        AddTag(onClick = { newTagText = TextFieldValue() })
        FilterTag(onClick = { tagFilter = TextFieldValue() })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Tag(
    text: String,
    isSelected: Boolean,
    isAntiSelected: Boolean = false,
    clickable: Boolean = true,
    onClick: () -> Unit = {},
    draggableTags: Boolean = true,
    onTagDrag: (String) -> Unit = {},
    onTagDrop: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Crossfade(
        isSelected,
        animationSpec = tween(normalAnimationDuration),
    ) { isS ->
        Crossfade(
            isAntiSelected,
            animationSpec = tween(normalAnimationDuration),
        ) { isAS ->
            Box(
                modifier = modifier
                    .clickable(clickable, onClick = onClick)
                    .padding(padding)
                    .background(
                        color = if (isS) colorBackgroundSecondLighter else if (isAS) colorBackgroundDelete else colorBackground,
                        shape = RoundedCornerShape(smallCorners)
                    )
                    .padding(padding)
                    .onDrag(
                        enabled = draggableTags,
                        onDragStart = { onTagDrag(text) },
                        onDrag = { onTagDrag(text) },
                        onDragEnd = { onTagDrop(text) },
                    )
            ) {
                Text(
                    text = text,
                    color = colorText,
                    fontSize = normalText,
                )
            }
        }
    }
}

@Composable
private fun TagField(
    fieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onFinish: () -> Unit,
    icon: ImageVector = Icons.Default.Check
) {
    Row(
        modifier = Modifier
            .padding(padding)
            .background(
                color = colorBackground,
                shape = RoundedCornerShape(smallCorners)
            )
            .padding(padding)
    ) {
        BasicTextField(
            value = fieldValue,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = colorText,
                fontSize = normalText,
            ),
            cursorBrush = SolidColor(colorText),
        )

        Icon(
            icon,
            "finish tag adding",
            tint = colorTextSuccess,
            modifier = Modifier
                .clickable(onClick = onFinish)
        )
    }
}

@Composable
private fun AddTag(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(padding)
            .background(
                color = colorBackground,
                shape = RoundedCornerShape(smallCorners)
            )
            .padding(padding)
    ) {
        Icon(
            Icons.Default.Add,
            "add new tag",
            tint = colorText,
        )
    }
}

@Composable
private fun FilterTag(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(padding)
            .background(
                color = colorBackground,
                shape = RoundedCornerShape(smallCorners)
            )
            .padding(padding)
    ) {
        Icon(
            Icons.Default.Search,
            "filter tags",
            tint = colorText,
        )
    }
}