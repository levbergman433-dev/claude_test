package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable

@Composable
actual fun ImmersiveSystemBars() {
    // A desktop window has no system bars to hide.
}

@Composable
actual fun ShowOverLockScreen() {
    // A desktop has no lock screen an app can draw over.
}

@Composable
actual fun rememberIs24HourClock(): Boolean = true
