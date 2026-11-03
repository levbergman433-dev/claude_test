package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/**
 * A 0..1 phase that loops linearly every [periodMillis] — but ONLY while [active] is true.
 *
 * This replaces `rememberInfiniteTransition` for decorations that are visible only some of the
 * time (the "Crossfading" shimmer, the chip border spin). An infinite transition keeps requesting
 * frames for as long as it is composed, whether or not anything on screen depends on it, so a
 * shimmer declared "unconditionally" (to avoid restarting from zero when the label reappears) kept
 * the whole Now Playing screen — and the MiniPlayer — drawing at the display's refresh rate for
 * the entire session, i.e. constant GPU work and a CPU that never idles between vsyncs.
 *
 * Here the phase simply freezes when [active] turns false and continues from the SAME value when
 * it turns true again, so the sweep never jumps — which was the only reason those call sites ran
 * unconditionally in the first place.
 *
 * Frames come from `withInfiniteAnimationFrameNanos`, the call Compose routes through the
 * InfiniteAnimationPolicy: on Desktop that is DesktopFrameGovernor, which paces these loops to 30fps
 * while no window has focus and stops them while every window is hidden. Chained `Animatable`
 * tweens (what this used to be) take their frames from `withFrameNanos` and slipped past it. With
 * animations turned off in the system (an animator duration scale of 0) the phase holds still.
 */
@Composable
fun rememberLoopingPhase(
    active: Boolean,
    periodMillis: Int = 3200,
): State<Float> {
    val phase = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(active, periodMillis) {
        if (!active || animationsDisabled()) return@LaunchedEffect
        val periodNanos = periodMillis.coerceAtLeast(1) * NANOS_PER_MILLI
        // Resume from where the phase was left, at the same speed.
        val start = phase.floatValue.toDouble()
        var origin = -1L
        while (isActive) {
            withInfiniteAnimationFrameNanos { now ->
                if (origin < 0L) origin = now
                phase.floatValue = ((start + (now - origin) / periodNanos) % 1.0).toFloat()
            }
        }
    }
    return phase
}

/**
 * Travels back and forth between [from] and [to], taking [durationMillis] per leg — the
 * `RepeatMode.Reverse` counterpart of [rememberLoopingPhase], with the same pause/resume
 * behaviour: while [active] is false the value holds still (and requests no frames), and it picks
 * up in the direction it was heading when it resumes.
 */
@Composable
fun rememberPingPong(
    active: Boolean,
    from: Float,
    to: Float,
    durationMillis: Int,
): State<Float> {
    val value = remember { mutableFloatStateOf(from) }
    // Where in a there-and-back cycle the value is: 0..1 heading to [to], 1..2 heading back.
    val cycle = remember { floatArrayOf(0f) }
    LaunchedEffect(active, from, to, durationMillis) {
        if (!active || animationsDisabled()) return@LaunchedEffect
        val legNanos = durationMillis.coerceAtLeast(1) * NANOS_PER_MILLI
        val start = cycle[0].toDouble()
        var origin = -1L
        while (isActive) {
            withInfiniteAnimationFrameNanos { now ->
                if (origin < 0L) origin = now
                val c = ((start + (now - origin) / legNanos) % 2.0).toFloat()
                cycle[0] = c
                val fraction = if (c < 1f) c else 2f - c
                value.floatValue = from + (to - from) * fraction
            }
        }
    }
    return value
}

private suspend fun animationsDisabled(): Boolean = coroutineContext[MotionDurationScale]?.scaleFactor == 0f

private const val NANOS_PER_MILLI = 1_000_000.0
