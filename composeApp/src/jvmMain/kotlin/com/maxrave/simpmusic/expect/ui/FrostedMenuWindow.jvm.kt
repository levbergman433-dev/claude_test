package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset

// A Desktop dialog is a separate OS window with its own frame, so menus stay in-window popups here.
@Composable
actual fun rememberFrostedMenuSupported(): Boolean = false

@Composable
actual fun FrostedMenuWindow(
    anchor: Rect,
    offset: DpOffset,
    cornerRadius: Dp,
    blurRadius: Dp,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    // Unreachable: rememberFrostedMenuSupported() is false on Desktop.
}