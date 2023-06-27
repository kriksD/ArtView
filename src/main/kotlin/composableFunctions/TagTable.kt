package composableFunctions

import TagCategory
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import colorBackground
import colorBackgroundDelete
import colorBackgroundSecondLighter
import colorText
import colorTextSuccess
import iconSize
import normalAnimationDuration
import normalText
import padding
import smallCorners
import smallText

@Composable
fun TagTableWithCategories(
    tags: List<TagCategory>,
    selectedTags: List<String> = emptyList(),
    antiSelectedTags: List<String> = emptyList(),
    controls: Boolean = true,
    expandable: Boolean = true,
    expanded: Boolean = true,
    onExpandedChange: (Boolean) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onNew: (name: String, category: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    if (expanded) {
        Box(
            modifier = modifier
        ) {
            Column(modifier = modifier) {
                tags.forEach { category ->
                    var categoryExpanded by remember { mutableStateOf(false) }
                    Text("${category.name}:", color = colorText, fontSize = normalText)
                    TagTable(
                        category.tags,
                        selectedTags,
                        antiSelectedTags,
                        controls,
                        expandable,
                        categoryExpanded,
                        onExpandedChange = { categoryExpanded = it },
                        onTagClick = onTagClick,
                        onNew = { onNew(it, category.name) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
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
                    onNew = { onNew(it, "Other") },
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
    expandable: Boolean = true,
    expanded: Boolean = true,
    onExpandedChange: (Boolean) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onNew: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (expanded) {
        Box(
            modifier = modifier
        ) {
            FlowRow(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AllTags(
                    tags = tags,
                    selectedTags = selectedTags,
                    antiSelectedTags = antiSelectedTags,
                    controls = controls,
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
            onClick = { onTagClick(tag) }
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

@Composable
private fun Tag(
    text: String,
    isSelected: Boolean,
    isAntiSelected: Boolean = false,
    clickable: Boolean = true,
    onClick: () -> Unit = {},
) {
    Crossfade(
        isSelected,
        animationSpec = tween(normalAnimationDuration),
    ) { iss ->
        Crossfade(
            isAntiSelected,
            animationSpec = tween(normalAnimationDuration),
        ) { isas ->
            Box(
                modifier = Modifier
                    .clickable(clickable, onClick = onClick)
                    .padding(padding)
                    .background(
                        color = if (iss) colorBackgroundSecondLighter else if (isas) colorBackgroundDelete else colorBackground,
                        shape = RoundedCornerShape(smallCorners)
                    )
                    .padding(padding)
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