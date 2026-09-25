package com.maxrave.simpmusic.expect.ui

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.roundToInt

// External volume changes (hardware keys, another app) are picked up through a ContentObserver on
// Settings.System: the audio service writes every stream volume there, so the observer fires on
// each change and stays silent otherwise. This replaced a 1 s poll that woke the main thread for
// as long as the Apple Music player was open.

private fun AudioManager.currentVolumeFraction(): Float {
    val max = getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    if (max <= 0) return 0f
    return getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max
}

@Composable
actual fun rememberDeviceVolumeController(): DeviceVolumeController? {
    val context = LocalContext.current
    val audioManager =
        remember(context) {
            context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        } ?: return null

    // Backing state for `DeviceVolumeController.volumeFraction` — captured by the object below
    // so a write from `setVolumeFraction` is reflected immediately, without waiting on the observer.
    var fraction by remember(audioManager) { mutableFloatStateOf(audioManager.currentVolumeFraction()) }

    DisposableEffect(context, audioManager) {
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    // Only adopt changes to the actual volume STEP. Our own setVolumeFraction also
                    // lands here, and overwriting the finger's exact fraction with the quantised
                    // step would make the slider thumb snap while it is being dragged.
                    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val actual = audioManager.currentVolumeFraction()
                    if ((fraction * max).roundToInt() != (actual * max).roundToInt()) {
                        fraction = actual
                    }
                }
            }
        val resolver = context.contentResolver
        resolver.registerContentObserver(Settings.System.CONTENT_URI, true, observer)
        // Catch anything that changed between the initial read and the registration.
        fraction = audioManager.currentVolumeFraction()
        onDispose { resolver.unregisterContentObserver(observer) }
    }

    return remember(audioManager) {
        object : DeviceVolumeController {
            override val volumeFraction: Float get() = fraction

            override fun setVolumeFraction(newFraction: Float) {
                val clamped = newFraction.coerceIn(0f, 1f)
                val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                // FLAG 0 — no system volume UI flash; this row IS the volume UI.
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (clamped * max).roundToInt(), 0)
                // Reflect immediately rather than waiting on the observer, so the slider
                // doesn't lag behind the finger.
                fraction = clamped
            }
        }
    }
}
