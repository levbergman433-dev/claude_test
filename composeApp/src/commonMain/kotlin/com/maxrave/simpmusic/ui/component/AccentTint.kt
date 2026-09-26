package com.maxrave.simpmusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import com.maxrave.simpmusic.ui.theme.LocalAccentBrush

/**
 * Paints the gradient accent (Theme color > Gradient) over whatever this element draws, keeping
 * its shape: icons, labels, glyphs. Material components only take one colour, so an accent
 * gradient is applied by drawing the element in any colour and then replacing its pixels with the
 * gradient (SrcIn). A no-op when the accent is solid, so it can be used unconditionally on
 * anything tinted with the primary colour.
 */
@Composable
fun Modifier.accentTint(enabled: Boolean = true): Modifier {
    val brush = LocalAccentBrush.current
    if (!enabled || brush == null) return this
    return this
        // Offscreen, so SrcIn only sees this element's own pixels and not what is behind it.
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(brush = brush, blendMode = BlendMode.SrcIn)
        }
}
