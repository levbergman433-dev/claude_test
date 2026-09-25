package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.layer.GraphicsLayer
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.extension.toResizedBitmap
import com.maxrave.simpmusic.ui.theme.LocalBatterySaver
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs

private const val TAG = "BackdropLuminance"

private const val SAMPLE_INTERVAL_MS = 1_000L
private const val SAMPLE_INTERVAL_BATTERY_SAVER_MS = 4_000L

// Below this change the glass would re-tint by an invisible amount, so the 500 ms animation (and
// the frames it requests) is skipped entirely.
private const val MIN_VISIBLE_DELTA = 0.02f

/**
 * Average luminance (0.3..0.8) of whatever a glass surface records into [layer], re-sampled
 * periodically and animated, so glass can darken over bright artwork and lighten over dark pages.
 *
 * Shared by the MiniPlayer and the glass bottom bar, which each used to run their own copy of this
 * loop. Two things make it much cheaper than those copies:
 *  - every sample first awaits a frame. Compose pauses its frame clock while the window is not
 *    visible, so a backgrounded app neither reads back GPU layers nor spins the loop at all;
 *  - the re-tint animation only runs when the value actually moved. The old loops animated every
 *    second regardless, which held the frame clock busy half of the time on a static screen.
 *
 * With Battery saver on, samples are four times rarer.
 */
@Composable
fun rememberBackdropLuminance(
    layer: GraphicsLayer,
    enabled: Boolean = true,
): Animatable<Float, AnimationVector1D> {
    val luminance = remember { Animatable(0f) }
    val intervalMs = if (LocalBatterySaver.current) SAMPLE_INTERVAL_BATTERY_SAVER_MS else SAMPLE_INTERVAL_MS
    LaunchedEffect(layer, enabled, intervalMs) {
        if (!enabled) return@LaunchedEffect
        val buffer = IntArray(25)
        while (isActive) {
            withFrameNanos { }
            val sampled =
                try {
                    layer.toImageBitmap().toResizedBitmap(5, 5).readPixels(buffer)
                    averageLuminance(buffer)
                } catch (e: Exception) {
                    Logger.e(TAG, "Error getting pixels from layer: ${e.message}")
                    null
                }
            if (sampled != null) {
                val target = sampled.coerceIn(0.3f, 0.8f)
                if (abs(target - luminance.targetValue) > MIN_VISIBLE_DELTA) {
                    luminance.animateTo(target, tween(500))
                }
            }
            delay(intervalMs)
        }
    }
    return luminance
}

private fun averageLuminance(pixels: IntArray): Float {
    var sum = 0f
    for (color in pixels) {
        val r = (color shr 16 and 0xFF) / 255f
        val g = (color shr 8 and 0xFF) / 255f
        val b = (color and 0xFF) / 255f
        sum += 0.2126f * r + 0.7152f * g + 0.0722f * b
    }
    return sum / pixels.size
}
