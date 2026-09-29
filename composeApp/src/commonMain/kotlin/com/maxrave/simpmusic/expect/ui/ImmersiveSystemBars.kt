package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable

/**
 * Hides the status and navigation bars while this is in composition, and brings them back when it
 * leaves. A swipe from the edge still shows them briefly. A no-op where there are no system bars.
 */
@Composable
expect fun ImmersiveSystemBars()

/**
 * While in composition, lets the app stay visible over the lock screen: press power to turn the
 * screen off and on again, and this page is shown without unlocking. A no-op where there is none.
 */
@Composable
expect fun ShowOverLockScreen()

/** Whether the device shows time in 24-hour format. */
@Composable
expect fun rememberIs24HourClock(): Boolean
