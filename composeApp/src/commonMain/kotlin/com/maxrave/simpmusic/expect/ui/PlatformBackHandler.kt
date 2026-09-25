package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable

/** Intercepts the system back gesture while [enabled]. A no-op where the platform has none. */
@Composable
expect fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
