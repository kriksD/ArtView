import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import properties.toColor

enum class MenuItem {
    Images, Favorites, Collections, Settings
}

@Composable
fun LeftSideMenu(
    selected: MenuItem,
    onOptionSelected: (MenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(colorBackground)
            .padding(vertical = biggerPadding)
    ) {
        MenuOption(
            icon = painterResource("image.svg"),
            selected = selected == MenuItem.Images,
            onClick = { onOptionSelected(MenuItem.Images) }
        )
        MenuOption(
            icon = Icons.Default.Favorite,
            selected = selected == MenuItem.Favorites,
            onClick = { onOptionSelected(MenuItem.Favorites) }
        )
        MenuOption(
            icon = painterResource("photo_library.svg"),
            selected = selected == MenuItem.Collections,
            onClick = { onOptionSelected(MenuItem.Collections) }
        )
        MenuOption(
            icon = Icons.Default.Settings,
            selected = selected == MenuItem.Settings,
            onClick = { onOptionSelected(MenuItem.Settings) }
        )
    }
}

@Composable
private fun MenuOption(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = colorText,
        modifier = Modifier
            .size(bigIconSize)
            .clickable(onClick = onClick)
            .background(if (selected) colorBackgroundSecond else Color.Transparent)
            .padding(horizontal = biggerPadding, vertical = padding)
    )
}

@Composable
private fun MenuOption(
    icon: Painter,
    selected: Boolean,
    onClick: () -> Unit
) {
    Icon(
        painter = icon,
        contentDescription = null,
        tint = colorText,
        modifier = Modifier
            .size(bigIconSize)
            .clickable(onClick = onClick)
            .background(if (selected) colorBackgroundSecond else Color.Transparent)
            .padding(horizontal = biggerPadding, vertical = padding)
    )
}