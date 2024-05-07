package composableFunctions

import ImageInfo
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import calculateWeight
import colorBackground
import emptyImageBitmap
import iconSize
import normalAnimationDuration
import smallCorners

@Composable
fun LoadingImage(
    imageInfo: ImageInfo,
    description: String = imageInfo.name,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Crossfade(
            imageInfo.isLoaded,
            animationSpec = tween(normalAnimationDuration),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (!it) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(imageInfo.calculateWeight())
                        .clip(RoundedCornerShape(smallCorners))
                        .background(colorBackground)
                )
                LoadingIcon(
                    contentDescription = "loading an image",
                    modifier = Modifier.size(iconSize).align(Alignment.Center)
                )
            } else {
                Image(
                    imageInfo.scaledDownImage ?: ImageBitmap(imageInfo.width, imageInfo.height),
                    description,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(imageInfo.calculateWeight())
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