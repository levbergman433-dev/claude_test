package com.maxrave.simpmusic.expect.ui

import android.app.Activity
import android.os.Build
import android.text.format.DateFormat
import android.view.WindowManager
import android.content.ContextWrapper
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun ImmersiveSystemBars() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.hostWindow() ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val previousBehavior = controller.systemBarsBehavior
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = previousBehavior
        }
    }
}

@Suppress("DEPRECATION")
@Composable
actual fun ShowOverLockScreen() {
    val view = LocalView.current
    DisposableEffect(view) {
        val activity = view.context.findActivity()
        val host = view.hostWindow()
        // The activity must be allowed over the keyguard, and so must the sheet window the player is
        // drawn in: a window of its own, which the activity flag alone does not cover.
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) activity.setShowWhenLocked(true)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        host?.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        onDispose {
            if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) activity.setShowWhenLocked(false)
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            host?.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
    }
}

@Composable
actual fun rememberIs24HourClock(): Boolean {
    val context = LocalContext.current
    return remember(context) { DateFormat.is24HourFormat(context) }
}

private fun android.content.Context.findActivity(): Activity? {
    var context: android.content.Context? = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

/**
 * The window this view is drawn in. The player lives in a bottom sheet, which is a window of its
 * own, so the bars must be hidden on that window rather than on the activity's.
 */
private fun View.hostWindow(): Window? {
    var parent = parent
    while (parent != null) {
        if (parent is DialogWindowProvider) return parent.window
        parent = parent.parent
    }
    return context.findActivity()?.window
}
