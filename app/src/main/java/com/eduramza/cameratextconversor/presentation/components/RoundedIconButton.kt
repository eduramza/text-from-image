package com.eduramza.cameratextconversor.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RoundedIconButton(
    color: Color,
    icon: ImageVector,
    borderWidth: Dp = 4.dp,
    buttonSize: Dp = 50.dp,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    OutlinedIconButton(
        modifier = Modifier
            .then(
                Modifier
                    .size(buttonSize)
                    .border(borderWidth, color, shape = CircleShape)
            ),
        onClick = { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color
        )
    }
}

@Preview
@Composable
fun PreviewRoundedIconButton() {
    RoundedIconButton(
        color = MaterialTheme.colorScheme.onPrimary,
        icon = Icons.Default.Camera,
        onClick = {}
    )
}