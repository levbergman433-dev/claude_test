package com.maxrave.simpmusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * Paints the slice of the page gradient that lies under this element, so a bar drawn with it is
 * indistinguishable from the page behind it.
 *
 * The page brush is painted once across the whole window. Filling a bar with the same brush
 * directly would stretch the entire gradient across the bar's own few dp; this instead draws the
 * brush at window size, shifted by the bar's position, and clips it to the bar.
 */
@Composable
fun Modifier.pageBrushSlice(brush: Brush): Modifier {
    var origin by remember { mutableStateOf(Offset.Zero) }
    val window = LocalWindowInfo.current.containerSize
    return this
        .onGloballyPositioned { origin = it.positionInWindow() }
        .drawBehind {
            clipRect {
                translate(-origin.x, -origin.y) {
                    drawRect(brush, size = Size(window.width.toFloat(), window.height.toFloat()))
                }
            }
        }
}
