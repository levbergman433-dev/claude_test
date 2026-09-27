package com.maxrave.simpmusic.ui.component

import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.HazeState
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.runtime.staticCompositionLocalOf
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

/** Top bar looks (Appearance > Top bar). [TOP_BAR_DEFAULT] keeps each screen's own behaviour. */
const val TOP_BAR_DEFAULT = "DEFAULT"
const val TOP_BAR_BLUR = "BLUR"
const val TOP_BAR_DIM = "DIM"
const val TOP_BAR_TRANSPARENT = "TRANSPARENT"

val LocalTopBarStyle = staticCompositionLocalOf { TOP_BAR_DEFAULT }

/**
 * The surface behind a page's top bar. With the default style the screen's own [default] is used
 * (the page colour or gradient slice the bar has always had); the other styles replace it once the
 * page has scrolled: a frosted blur of what is underneath, a translucent dim, or nothing at all.
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun Modifier.topBarSurface(
    atTop: Boolean,
    hazeState: HazeState,
    default: Modifier,
): Modifier {
    val style = LocalTopBarStyle.current
    if (style == TOP_BAR_DEFAULT) return this.then(default)
    if (atTop) return this
    return when (style) {
        TOP_BAR_BLUR -> this.hazeEffect(hazeState, style = HazeMaterials.ultraThin()) { blurEnabled = true }
        TOP_BAR_DIM -> this.background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f))
        TOP_BAR_TRANSPARENT -> this
        else -> this.then(default)
    }
}
