package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset

/**
 * Whether this platform can blur the screen behind a pop-up menu window. Android can from 12 on,
 * and only while the system allows cross-window blur (it is switched off in battery saver and on
 * some devices).
 */
@Composable
expect fun rememberFrostedMenuSupported(): Boolean

/**
 * Shows [content] in a window of its own, placed against [anchor] (window coordinates, px) the way
 * a dropdown menu is, with the screen behind that window blurred inside a [cornerRadius] outline.
 * Only called where [rememberFrostedMenuSupported] is true.
 */
@Composable
expect fun FrostedMenuWindow(
    anchor: Rect,
    offset: DpOffset,
    cornerRadius: Dp,
    blurRadius: Dp,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
)