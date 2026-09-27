package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.expect.ui.FrostedMenuWindow
import com.maxrave.simpmusic.expect.ui.rememberFrostedMenuSupported
import com.maxrave.simpmusic.ui.theme.LocalIsDarkTheme

private val MenuShape = RoundedCornerShape(24.dp)

/**
 * A pop-up menu in the liquid-glass look, and a drop-in replacement for [DropdownMenu].
 *
 * Where the system allows it (Android 12+ with cross-window blur on), the menu is its own small
 * window with the screen behind it genuinely blurred, so it is frosted glass like every other glass
 * surface in the app rather than a tinted sheet you can read the page through. It carries the same
 * finish as those surfaces: a light tint, a rim lit on the top-left and bottom-right like the
 * shared Apple highlight, and a sheen across the top.
 *
 * Elsewhere it falls back to an ordinary popup with a denser body, since without the blur a
 * see-through menu shows the covers and text behind it sharply.
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
    // Menu style "Liquid glass": drawn in-window by GlassMenuOverlay as the same glass as the nav
    // bar. The host is only provided while that style is chosen and the page is being recorded.
    val host = LocalGlassMenuHost.current
    if (host != null) {
        LiquidGlassMenu(host, expanded, onDismissRequest, modifier, offset, content)
        return
    }
    if (!rememberFrostedMenuSupported()) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            offset = offset,
            shape = MenuShape,
            containerColor = if (dark) Color(0xFF232327).copy(alpha = 0.96f) else Color(0xFFF7F7FA).copy(alpha = 0.97f),
            tonalElevation = 0.dp,
            shadowElevation = 18.dp,
            border = BorderStroke(0.8.dp, glassRim(dark)),
            modifier = modifier.drawBehind { drawRect(glassSheen(dark, size.height)) },
            content = content,
        )
        return
    }

    // Where the anchor is: the same zero-size probe a Popup places in its parent to find the
    // element it belongs to.
    var anchor by remember { mutableStateOf<Rect?>(null) }
    Layout(
        content = {},
        modifier =
            Modifier.onGloballyPositioned { coordinates ->
                anchor = coordinates.parentLayoutCoordinates?.boundsInWindow()
            },
    ) { _, _ -> layout(0, 0) {} }

    val anchorRect = anchor
    if (expanded && anchorRect != null) {
        FrostedMenuWindow(
            anchor = anchorRect,
            offset = offset,
            cornerRadius = 24.dp,
            blurRadius = 36.dp,
            onDismissRequest = onDismissRequest,
        ) {
            val appear = remember { Animatable(0f) }
            LaunchedEffect(Unit) { appear.animateTo(1f, tween(140)) }
            Column(
                modifier =
                    modifier
                        .graphicsLayer { alpha = appear.value }
                        .width(IntrinsicSize.Max)
                        .heightIn(max = 520.dp)
                        .clip(MenuShape)
                        // Light, because the blur now does the frosting: the same weight of tint the
                        // glass buttons carry, not the near-opaque body a blur-less menu needs.
                        .background(if (dark) Color.Black.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.42f))
                        .drawBehind { drawRect(glassSheen(dark, size.height)) }
                        .border(1.dp, glassRim(dark), MenuShape)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                content = content,
            )
        }
    }
}

/**
 * The rim of the shared Apple glass: bright on the top-left, fading through the middle and catching
 * light again on the bottom-right — the same directional sweep the glass buttons' highlight draws.
 */
private fun glassRim(dark: Boolean): Brush =
    Brush.linearGradient(
        0f to Color.White.copy(alpha = if (dark) 0.5f else 0.95f),
        0.45f to Color.White.copy(alpha = if (dark) 0.06f else 0.3f),
        1f to Color.White.copy(alpha = if (dark) 0.28f else 0.7f),
        start = Offset.Zero,
        end = Offset.Infinite,
    )

private fun glassSheen(
    dark: Boolean,
    height: Float,
): Brush =
    Brush.verticalGradient(
        0f to Color.White.copy(alpha = if (dark) 0.08f else 0.22f),
        (56f * 3f / height.coerceAtLeast(1f)).coerceIn(0.05f, 0.6f) to Color.Transparent,
    )

/** Registers this menu with [host] while it is open; [GlassMenuOverlay] draws it. */
@Composable
private fun LiquidGlassMenu(
    host: GlassMenuHost,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    offset: DpOffset,
    content: @Composable ColumnScope.() -> Unit,
) {
    var anchor by remember { mutableStateOf<Rect?>(null) }
    Layout(
        content = {},
        modifier =
            Modifier.onGloballyPositioned { coordinates ->
                anchor = coordinates.parentLayoutCoordinates?.boundsInWindow()
            },
    ) { _, _ -> layout(0, 0) {} }

    // The overlay composes the menu far from here, so it must always call the LATEST content and
    // dismiss lambdas, not the ones captured when the menu opened.
    val latestContent by rememberUpdatedState(content)
    val latestDismiss by rememberUpdatedState(onDismissRequest)
    val token = remember { Any() }
    val anchorRect = anchor
    DisposableEffect(host, expanded, anchorRect) {
        if (expanded && anchorRect != null) {
            host.show(
                ActiveGlassMenu(
                    token = token,
                    anchor = anchorRect,
                    offset = offset,
                    modifier = modifier,
                    onDismissRequest = { latestDismiss() },
                    content = { latestContent() },
                ),
            )
        } else {
            host.hide(token)
        }
        onDispose { host.hide(token) }
    }
}
