package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.theme.LocalIsDarkTheme

/**
 * A pop-up menu in the liquid-glass look: a translucent tinted body, a specular rim that is bright
 * along the top edge and fades down the sides, a soft sheen across the top rows, and large rounded
 * corners.
 *
 * A menu is drawn in its own pop-up window, above the app's backdrop layer, so it cannot refract
 * the page the way the glass buttons do; this reproduces the look of the material rather than the
 * lens. A drop-in replacement for [DropdownMenu].
 */
@Composable
fun GlassDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = LocalIsDarkTheme.current
    val body = if (dark) Color(0xFF26262B).copy(alpha = 0.78f) else Color(0xFFF7F7FA).copy(alpha = 0.82f)
    val rim =
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (dark) 0.42f else 0.9f),
                Color.White.copy(alpha = if (dark) 0.06f else 0.35f),
            ),
        )
    val sheen = Color.White.copy(alpha = if (dark) 0.07f else 0.25f)
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        shape = RoundedCornerShape(22.dp),
        containerColor = body,
        tonalElevation = 0.dp,
        shadowElevation = 18.dp,
        border = BorderStroke(0.8.dp, rim),
        modifier =
            modifier.drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        listOf(sheen, Color.Transparent),
                        endY = 56.dp.toPx(),
                    ),
                )
            },
        content = content,
    )
}
