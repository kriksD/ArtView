package composableFunctions

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import colorBackgroundSecondLighter
import colorText
import normalText

@Composable
fun ButtonText(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(backgroundColor = colorBackgroundSecondLighter),
        modifier = modifier,
    ) {
        Text(text, color = colorText, fontSize = normalText)
    }
}