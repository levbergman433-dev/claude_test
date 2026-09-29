package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable

/**
 * Hides the status and navigation bars while this is in composition, and brings them back when it
 * leaves. A swipe from the edge still shows them briefly. A no-op where there are no system bars.
 */
@Composable
expect fun ImmersiveSystemBars()
