package com.maxrave.common

import kotlin.concurrent.Volatile

/**
 * How much bandwidth the connection is actually delivering, as measured by the player.
 *
 * On a weak connection a 256 kbps (or even 129 kbps) stream cannot be sustained: it stalls every
 * few seconds, and the Premium-quality extraction path it needs is itself slower. Apps like YouTube
 * and Spotify adapt the bitrate; this is what lets stream selection do the same.
 */
object NetworkQuality {
    /** Preference key (plain string pref, "TRUE"/"FALSE", default on) for dropping quality on a weak connection. */
    const val ADAPTIVE_QUALITY_KEY = "adaptive_quality_weak_network"

    /**
     * Below this the connection is treated as weak. The 66 kbps stream needs roughly five times its
     * bitrate to start quickly and not stall, the 129/256 kbps ones proportionally more.
     */
    const val WEAK_BITRATE_BPS: Long = 450_000L

    /**
     * Supplies the player's current bandwidth estimate in bits per second. Set by the Android
     * player (ExoPlayer's shared bandwidth meter, which measures every stream transfer); null where
     * nothing measures it, in which case the connection is never considered weak.
     */
    @Volatile
    var bitrateSupplier: (() -> Long)? = null

    val isWeak: Boolean
        get() {
            val estimate = runCatching { bitrateSupplier?.invoke() }.getOrNull() ?: return false
            return estimate in 1 until WEAK_BITRATE_BPS
        }
}