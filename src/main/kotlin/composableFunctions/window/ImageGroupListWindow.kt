package composableFunctions.window

import info.group.MediaGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import border
import colorBackgroundSecond
import colorText
import composableFunctions.views.ButtonText
import composableFunctions.views.LoadingImage
import corners
import mediaData
import normalText
import padding
import smallCorners

@Composable
fun ImageGroupListWindow(
    onDone: (MediaGroup) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        var selectedGroup by remember { mutableStateOf<MediaGroup?>(null) }

        Column(
            modifier = Modifier.weight(1F).verticalScroll(rememberScrollState()),
        ) {
            mediaData.mediaGroups.forEach { imageGroup ->
                ImageGroupListItem(
                    mediaGroup = imageGroup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            border,
                            colorBackgroundSecond.copy(if (selectedGroup == imageGroup) 1F else 0F),
                            RoundedCornerShape(corners)
                        )
                        .clickable { selectedGroup = imageGroup },
                )
            }
        }

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
                    selectedGroup?.let { onDone(it) }
                },
            )
        }
    }
}

@Composable
private fun ImageGroupListItem(
    mediaGroup: MediaGroup,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(0.2F).aspectRatio(1F),
        ) {
            mediaGroup.getMediaInfo(0)?.let {
                LoadingImage(
                    it,
                    mediaGroup.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(smallCorners)),
                )
            }
        }

        Column {
            Text(mediaGroup.name, color = colorText, fontSize = normalText)
            Text(mediaGroup.description, color = colorText, fontSize = normalText)
        }
    }
}