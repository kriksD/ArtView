package composableFunctions.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import bigIconSize
import biggerPadding
import calculateWeight
import colorBackground
import colorText
import colorTextError
import emptyImageBitmap
import iconSize
import info.media.AudioInfo
import info.media.MediaInfo
import normalAnimationDuration
import smallCorners

@Composable
fun LoadingImage(
    mediaInfo: MediaInfo,
    description: String = mediaInfo.name,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Crossfade(
            mediaInfo.isThumbnailLoaded,
            animationSpec = tween(normalAnimationDuration),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (mediaInfo.path.contains("[Error: File not found]")) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(mediaInfo.thumbnailWeight())
                        .clip(RoundedCornerShape(smallCorners))
                        .background(colorBackground)
                )
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "Error: file not found",
                    tint = colorTextError,
                    modifier = Modifier.size(iconSize).align(Alignment.Center)
                )

            } else if (mediaInfo is AudioInfo && mediaInfo.thumbnailWidth == null && mediaInfo.thumbnailHeight == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1F)
                        .clip(RoundedCornerShape(smallCorners))
                        .background(colorBackground),
                )

                Icon(
                    painter = painterResource("volume_up.svg"),
                    contentDescription = mediaInfo.name,
                    tint = colorText,
                    modifier = Modifier
                        .background(colorBackground)
                        .padding(biggerPadding)
                        .size(bigIconSize)
                        .align(Alignment.Center),
                )
            } else if (!it) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(mediaInfo.thumbnailWeight())
                        .clip(RoundedCornerShape(smallCorners))
                        .background(colorBackground)
                )
                LoadingIcon(
                    contentDescription = "loading an image",
                    modifier = Modifier.size(iconSize).align(Alignment.Center)
                )
            } else {
                Image(
                    mediaInfo.thumbnail ?: emptyImageBitmap,
                    description,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(mediaInfo.thumbnailWeight())
                        .clip(RoundedCornerShape(smallCorners))
                        .background(colorBackground),
                )
            }
        }
    }
}

@Composable
fun LoadingImage(
    image: ImageBitmap?,
    description: String = "",
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Crossfade(
            image != null,
            animationSpec = tween(normalAnimationDuration),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (!it) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(image?.calculateWeight() ?: 1F)
                        .clip(RoundedCornerShape(smallCorners))
                        .background(colorBackground)
                )
                LoadingIcon(
                    contentDescription = "loading an image",
                    modifier = Modifier.size(iconSize).align(Alignment.Center)
                )
            } else {
                Image(
                    image ?: emptyImageBitmap,
                    description,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(smallCorners))
                        .background(colorBackground),
                )
            }
        }
    }
}