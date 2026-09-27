package com.maxrave.simpmusic.expect.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlin.math.roundToInt

@Composable
actual fun rememberFrostedMenuSupported(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.isCrossWindowBlurEnabled == true
    }
}

/**
 * A Dialog rather than a Popup, because only a Window can ask the compositor to blur what is
 * behind it (Window.setBackgroundBlurRadius). The window is shrunk to the menu, placed where a
 * dropdown would sit, given no dim, and a transparent background whose corner radius is what
 * clips the blur to the menu's rounded outline.
 */
@Composable
actual fun FrostedMenuWindow(
    anchor: Rect,
    offset: DpOffset,
    cornerRadius: Dp,
    blurRadius: Dp,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val view = LocalView.current
        val density = LocalDensity.current
        val window = (view.parent as? DialogWindowProvider)?.window
        var menuSize by remember { mutableStateOf(IntSize.Zero) }
        var positioned by remember { mutableStateOf(false) }

        DisposableEffect(window) {
            window?.apply {
                setDimAmount(0f)
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
                setWindowAnimations(0)
                setBackgroundDrawable(
                    GradientDrawable().apply {
                        this.cornerRadius = with(density) { cornerRadius.toPx() }
                        setColor(android.graphics.Color.TRANSPARENT)
                    },
                )
            }
            onDispose { }
        }

        LaunchedEffect(window, menuSize, anchor) {
            val w = window ?: return@LaunchedEffect
            if (menuSize == IntSize.Zero) return@LaunchedEffect
            val bounds =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    w.windowManager.currentWindowMetrics.bounds
                } else {
                    null
                }
            val screenW = bounds?.width() ?: view.resources.displayMetrics.widthPixels
            val screenH = bounds?.height() ?: view.resources.displayMetrics.heightPixels
            val offX = with(density) { offset.x.roundToPx() }
            val offY = with(density) { offset.y.roundToPx() }
            // Same placement rule as a dropdown: start-aligned to the anchor when it fits,
            // otherwise end-aligned (the ⋮ at the right edge), below it unless that runs off screen.
            val x =
                (
                    if (anchor.left + menuSize.width <= screenW) {
                        anchor.left.roundToInt() + offX
                    } else {
                        anchor.right.roundToInt() - menuSize.width - offX
                    }
                ).coerceIn(0, (screenW - menuSize.width).coerceAtLeast(0))
            val below = anchor.bottom.roundToInt() + offY
            val y =
                if (below + menuSize.height <= screenH) {
                    below
                } else {
                    (anchor.top.roundToInt() - menuSize.height - offY).coerceAtLeast(0)
                }
            w.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
            w.attributes =
                w.attributes.apply {
                    gravity = Gravity.TOP or Gravity.START
                    this.x = x
                    this.y = y
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) fitInsetsTypes = 0
                }
            // Only now, once the window sits on the anchor: switched on earlier, the blur flashes as
            // a frosted square in the middle of the screen for the frame before it is moved.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                w.setBackgroundBlurRadius(with(density) { blurRadius.toPx() }.roundToInt())
            }
            positioned = true
        }

        // Hidden for the one frame the dialog spends centred before it is placed.
        Box(modifier = Modifier.onSizeChanged { menuSize = it }.alpha(if (positioned) 1f else 0f)) {
            content()
        }
    }
}