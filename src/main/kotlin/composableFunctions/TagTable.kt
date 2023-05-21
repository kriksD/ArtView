package composableFunctions

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagTable(
    tags: List<String>,
    selectedTags: List<String>,
    antiSelectedTags: List<String>,
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
            FlowRow {
                AllTags(
                    tags = tags,
                    selectedTags = selectedTags,
                    antiSelectedTags = antiSelectedTags,
                    onTagClick = { tag -> onTagClick(tag) },
                    onNew = onNew
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BasicTagTable(
    tags: List<String>,
    expanded: Boolean = true,
    onExpandedChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (expanded) {
        Box {
            FlowRow(modifier = modifier) {
                tags.forEach { tag ->
                    Tag(
                        text = tag,
                        isSelected = false,
                        clickable = false,
                    )
                }
            }

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
    } else {
        Row {
            Row(
                modifier = modifier
                    .horizontalScroll(rememberScrollState())
                    .weight(1F)
            ) {
                tags.forEach { tag ->
                    Tag(
                        text = tag,
                        isSelected = false,
                        clickable = false,
                    )
                }
            }

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

@Composable
private fun AllTags(
    tags: List<String>,
    selectedTags: List<String>,
    antiSelectedTags: List<String>,
    onTagClick: (String) -> Unit,
    onNew: (String) -> Unit,
) {
    var newTagText by remember { mutableStateOf<TextFieldValue?>(null) }
    var tagFilter by remember { mutableStateOf<TextFieldValue?>(null) }

    tags.filter { it.contains(tagFilter?.text.orEmpty(), true) }.forEach { tag ->
        Tag(
            text = tag,
            isSelected = tag in selectedTags,
            isAntiSelected = tag in antiSelectedTags,
            onClick = { onTagClick(tag) }
        )
    }

    AppearDisappearAnimation(
        newTagText != null,
        normalAnimationDuration,
    ) {
        TagFieled(
            newTagText ?: TextFieldValue(),
            onValueChange = { newTagText = it },
            onFinish = {
                onNew(newTagText?.text ?: "")
                newTagText = null
            }
        )
    }

    AppearDisappearAnimation(
        tagFilter != null,
        normalAnimationDuration,
    ) {
        TagFieled(
            tagFilter ?: TextFieldValue(),
            onValueChange = { tagFilter = it },
            onFinish = { tagFilter = null }
        )
    }

    AddTag(onClick = { newTagText = TextFieldValue() })
    FilterTag(onClick = { tagFilter = TextFieldValue() })
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
private fun TagFieled(
    fieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onFinish: () -> Unit,
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
            Icons.Default.Check,
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