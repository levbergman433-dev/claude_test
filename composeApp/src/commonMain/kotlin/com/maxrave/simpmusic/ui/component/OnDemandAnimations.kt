package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlinx.coroutines.isActive
import kotlin.math.abs

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
 */
@Composable
fun rememberLoopingPhase(
    active: Boolean,
    periodMillis: Int = 3200,
): State<Float> {
    val phase = remember { Animatable(0f) }
    LaunchedEffect(active, periodMillis) {
        if (!active) return@LaunchedEffect
        while (isActive) {
            // Resume mid-cycle with the remaining share of the period, keeping the speed constant.
            val remaining = ((1f - phase.value) * periodMillis).toInt().coerceAtLeast(1)
            phase.animateTo(1f, tween(durationMillis = remaining, easing = LinearEasing))
            phase.snapTo(0f)
        }
    }
    return phase.asState()
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
    val value = remember { Animatable(from) }
    // Direction survives pauses: [0] is true while heading towards [to].
    val headingToEnd = remember { booleanArrayOf(true) }
    LaunchedEffect(active, from, to, durationMillis) {
        if (!active) return@LaunchedEffect
        val span = abs(to - from).coerceAtLeast(Float.MIN_VALUE)
        while (isActive) {
            val target = if (headingToEnd[0]) to else from
            val remaining = (abs(target - value.value) / span * durationMillis).toInt().coerceAtLeast(1)
            value.animateTo(target, tween(durationMillis = remaining, easing = LinearEasing))
            headingToEnd[0] = !headingToEnd[0]
        }
    }
    return value.asState()
}
