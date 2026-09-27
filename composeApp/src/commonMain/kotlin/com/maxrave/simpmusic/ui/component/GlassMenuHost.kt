package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.maxrave.simpmusic.expect.ui.PlatformBackHandler
import kotlin.math.roundToInt

/**
 * Lets a [GlassDropdownMenu] open as REAL liquid glass — the same lens, blur and rim as the nav bar
 * and mini player.
 *
 * That glass can only refract the page layer inside the main window, and a normal dropdown opens
 * in a pop-up window of its own where that layer does not exist. So while a host is provided, the
 * menu is not a pop-up: it is drawn by [GlassMenuOverlay], which sits at the root of the app next
 * to the nav bar, a sibling of the page's backdrop source (never inside it, which would feed the
 * glass back into itself).
 *
 * Provided only when the menu style setting is Liquid glass AND the page is actually being
 * recorded as a backdrop (the liquid-glass setting is on); otherwise menus stay pop-ups.
 */
@Stable
class GlassMenuHost(
    val backdrop: Backdrop,
) {
    internal var active by mutableStateOf<ActiveGlassMenu?>(null)

    internal fun show(menu: ActiveGlassMenu) {
        active = menu
    }

    internal fun hide(token: Any) {
        if (active?.token === token) active = null
    }
}

internal class ActiveGlassMenu(
    val token: Any,
    val anchor: Rect,
    val offset: DpOffset,
    val modifier: Modifier,
    val onDismissRequest: () -> Unit,
    val content: @Composable ColumnScope.() -> Unit,
)

val LocalGlassMenuHost = staticCompositionLocalOf<GlassMenuHost?> { null }

private val GlassMenuShape = RoundedCornerShape(24.dp)

/** Draws the open menu, if any. Place it at the app root, after the scaffold, outside the backdrop source. */
@Composable
fun GlassMenuOverlay(host: GlassMenuHost) {
    val menu = host.active ?: return
    var origin by remember { mutableStateOf(Offset.Zero) }
    PlatformBackHandler(enabled = true) { menu.onDismissRequest() }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { origin = it.positionInWindow() },
    ) {
        // Tapping anywhere outside the menu closes it, like the pop-up it replaces. A separate
        // sibling UNDER the menu, so a tap on the menu itself never reaches it.
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(menu) { detectTapGestures { menu.onDismissRequest() } },
        )
        val appear = remember(menu.token) { Animatable(0f) }
        LaunchedEffect(menu.token) { appear.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 700f)) }
        var opensUpward by remember(menu.token) { mutableStateOf(false) }
        var alignedToEnd by remember(menu.token) { mutableStateOf(false) }
        Layout(
            content = {
                Column(
                    modifier =
                        menu.modifier
                            .graphicsLayer {
                                val p = appear.value
                                alpha = p.coerceIn(0f, 1f)
                                scaleX = 0.9f + 0.1f * p
                                scaleY = 0.9f + 0.1f * p
                                // Grows out of the corner nearest the button that opened it.
                                transformOrigin =
                                    TransformOrigin(if (alignedToEnd) 1f else 0f, if (opensUpward) 1f else 0f)
                            }.width(IntrinsicSize.Max)
                            .heightIn(max = 520.dp)
                            .liquidGlass(host.backdrop, GlassMenuShape, interactive = false)
                            .clip(GlassMenuShape)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                    content = menu.content,
                )
            },
        ) { measurables, constraints ->
            val placeable = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
            val anchorLeft = (menu.anchor.left - origin.x).roundToInt()
            val anchorRight = (menu.anchor.right - origin.x).roundToInt()
            val anchorTop = (menu.anchor.top - origin.y).roundToInt()
            val anchorBottom = (menu.anchor.bottom - origin.y).roundToInt()
            val offX = menu.offset.x.roundToPx()
            val offY = menu.offset.y.roundToPx()
            val gap = 6.dp.roundToPx()
            // Dropdown placement: start-aligned with the anchor when it fits, else end-aligned (the
            // ⋮ at the right edge); below it unless that runs off the bottom.
            val endAligned = anchorLeft + placeable.width > constraints.maxWidth
            val x =
                (if (endAligned) anchorRight - placeable.width - offX else anchorLeft + offX)
                    .coerceIn(0, (constraints.maxWidth - placeable.width).coerceAtLeast(0))
            val below = anchorBottom + offY + gap
            val upward = below + placeable.height > constraints.maxHeight
            val y =
                if (upward) {
                    (anchorTop - placeable.height - offY - gap).coerceAtLeast(0)
                } else {
                    below
                }
            alignedToEnd = endAligned
            opensUpward = upward
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(x, y)
            }
        }
    }
}
