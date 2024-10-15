package composableFunctions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import bigIconSize
import biggerPadding
import colorBackground
import colorBackgroundSecond
import colorText
import padding
import properties.DataFolder
import runCommand
import java.io.File

enum class MenuItem {
    Add, All, Favorites, Groups, Settings
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
            icon = Icons.Default.Add,
            selected = selected == MenuItem.Add,
            onClick = { onOptionSelected(MenuItem.Add) }
        )
        MenuOption(
            icon = painterResource("image.svg"),
            selected = selected == MenuItem.All,
            onClick = { onOptionSelected(MenuItem.All) }
        )
        MenuOption(
            icon = Icons.Default.Favorite,
            selected = selected == MenuItem.Favorites,
            onClick = { onOptionSelected(MenuItem.Favorites) }
        )
        MenuOption(
            icon = painterResource("photo_library.svg"),
            selected = selected == MenuItem.Groups,
            onClick = { onOptionSelected(MenuItem.Groups) }
        )
        MenuOption(
            icon = Icons.Default.Settings,
            selected = selected == MenuItem.Settings,
            onClick = { onOptionSelected(MenuItem.Settings) }
        )
        MenuOption(
            icon = painterResource("folder_open.svg"),
            selected = false,
            onClick = { "explorer \"${DataFolder.folder.absolutePath}\\\"".runCommand(File(".")) }
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