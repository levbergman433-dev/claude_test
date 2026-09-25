package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable

// Desktop has no system back gesture; the top bar's back arrow does the same job.
@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) = Unit
