package composableFunctions.window

import ImageGroup
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
import composableFunctions.ButtonText
import composableFunctions.LoadingImage
import corners
import normalText
import padding
import properties.Properties
import smallCorners

@Composable
fun ImageGroupListWindow(
    onDone: (ImageGroup) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        var selectedGroup by remember { mutableStateOf<ImageGroup?>(null) }

        Column(
            modifier = Modifier.weight(1F).verticalScroll(rememberScrollState()),
        ) {
            Properties.imagesData().imageGroups.forEach { imageGroup ->
                ImageGroupListItem(
                    imageGroup = imageGroup,
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
    imageGroup: ImageGroup,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(0.2F).aspectRatio(1F),
        ) {
            imageGroup.getImageInfo(0)?.let {
                LoadingImage(
                    it,
                    imageGroup.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(smallCorners)),
                )
            }
        }

        Column {
            Text(imageGroup.name, color = colorText, fontSize = normalText)
            Text(imageGroup.description, color = colorText, fontSize = normalText)
        }
    }
}