package composableFunctions

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import colorBackground
import colorBackgroundSecondLighter
import colorText
import info.media.MediaType
import normalAnimationDuration
import normalText
import padding
import smallCorners

@Composable
fun TypeSelector(
    selected: MediaType?,
    onSelected: (MediaType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        TypeOption(
            type = null,
            isSelected = selected == null,
            modifier = Modifier
                .clickable {
                    onSelected(null)
                },
        )
        MediaType.entries.forEach { type ->
            TypeOption(
                type = type,
                isSelected = type == selected,
                modifier = Modifier
                    .clickable {
                        onSelected(type)
                    },
            )
        }
    }
}

@Composable
private fun TypeOption(
    type: MediaType?,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        isSelected,
        animationSpec = tween(normalAnimationDuration),
    ) { isS ->
        Box(
            modifier = modifier
                .padding(padding)
                .background(
                    color = if (isS) colorBackgroundSecondLighter else colorBackground,
                    shape = RoundedCornerShape(smallCorners)
                )
                .padding(padding)
        ) {
            Text(
                text = type?.typeName ?: "All",
                color = colorText,
                fontSize = normalText,
            )
        }
    }
}