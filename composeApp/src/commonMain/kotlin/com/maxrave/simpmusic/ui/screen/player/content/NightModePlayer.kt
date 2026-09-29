package com.maxrave.simpmusic.ui.screen.player.content

import androidx.compose.animation.AnimatedVisibility
import com.maxrave.simpmusic.ui.theme.AOD_THEME_MINIMAL
import com.maxrave.simpmusic.ui.theme.AOD_THEME_ARTWORK
import com.maxrave.simpmusic.ui.theme.AOD_THEME_AMBIENT
import com.maxrave.simpmusic.ui.theme.AOD_THEME_CLASSIC
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.blur
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.simpmusic.expect.ui.ImmersiveSystemBars
import com.maxrave.simpmusic.expect.ui.PlatformBackHandler
import com.maxrave.simpmusic.expect.ui.ShowOverLockScreen
import com.maxrave.simpmusic.expect.ui.toImageBitmap
import com.maxrave.simpmusic.expect.ui.rememberIs24HourClock
import com.maxrave.simpmusic.extension.KeepScreenOn
import com.maxrave.simpmusic.extension.formatDuration
import com.maxrave.simpmusic.ui.icon.Close
import com.maxrave.simpmusic.ui.icon.FastForward
import com.maxrave.simpmusic.ui.icon.FastRewind
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.screen.home.analytics.monthShortName
import com.maxrave.simpmusic.ui.screen.player.content.applemusic.AppleMusicThinSlider
import com.maxrave.simpmusic.ui.theme.AOD_CLOCK_BOLD
import com.maxrave.simpmusic.ui.theme.AOD_CLOCK_GLASS
import com.maxrave.simpmusic.ui.theme.AOD_CLOCK_MINIMAL
import com.maxrave.simpmusic.ui.theme.AOD_CLOCK_THIN
import com.maxrave.simpmusic.ui.theme.PersonalizationKeys
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.UIEvent
import kotlinx.coroutines.delay
import androidx.compose.ui.input.pointer.PointerEventType
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val NightArtworkShape = RoundedCornerShape(10.dp)
private val NightSubtle = Color.White.copy(alpha = 0.55f)

// Burn-in protection: the whole page drifts within this box, one small step a minute, so no pixel
// stays lit in the same place for hours.
private const val SHIFT_RANGE_DP = 10
private const val SHIFT_INTERVAL_MS = 60_000L

// Auto-dim: how far the page fades once nobody has touched it for the chosen time.
private const val DIMMED_ALPHA = 0.3f

/**
 * The night-mode (AOD) player: a pure black page with an optional clock, the artwork, the track, a
 * seek bar and the three transport buttons — for a phone lying beside the bed or on a stand.
 *
 * While it is open the screen stays awake, the system bars are hidden, and (unless turned off in
 * Settings) it stays visible over the lock screen and drifts slowly against burn-in. Auto-dim is off
 * unless the user picks a delay. It sits over the whole player, whichever style is chosen, and
 * closes with the ✕ or back.
 */
@Composable
internal fun NightModePlayer(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    onClose: () -> Unit,
    dataStoreManager: DataStoreManager = koinInject(),
) {
    val clockPref by remember { dataStoreManager.getString(PersonalizationKeys.AOD_CLOCK) }.collectAsStateWithLifecycle(null)
    val clockStylePref by remember { dataStoreManager.getString(PersonalizationKeys.AOD_CLOCK_STYLE) }.collectAsStateWithLifecycle(null)
    val lockScreenPref by remember { dataStoreManager.getString(PersonalizationKeys.AOD_LOCK_SCREEN) }.collectAsStateWithLifecycle(null)
    val burnInPref by remember { dataStoreManager.getString(PersonalizationKeys.AOD_BURN_IN) }.collectAsStateWithLifecycle(null)
    val autoDimPref by remember { dataStoreManager.getString(PersonalizationKeys.AOD_AUTO_DIM) }.collectAsStateWithLifecycle(null)
    val themePref by remember { dataStoreManager.getString(PersonalizationKeys.AOD_THEME) }.collectAsStateWithLifecycle(null)
    val theme = themePref ?: AOD_THEME_CLASSIC
    val showClock = clockPref != DataStoreManager.FALSE
    val clockStyle = clockStylePref ?: AOD_CLOCK_GLASS
    val burnInProtection = burnInPref != DataStoreManager.FALSE
    val autoDimSeconds = autoDimPref?.toIntOrNull() ?: 0

    KeepScreenOn()
    ImmersiveSystemBars()
    if (lockScreenPref != DataStoreManager.FALSE) ShowOverLockScreen()
    PlatformBackHandler(enabled = true, onBack = onClose)

    // Burn-in drift, animated so the step is never a visible jump.
    val shiftX = remember { Animatable(0f) }
    val shiftY = remember { Animatable(0f) }
    LaunchedEffect(burnInProtection) {
        if (!burnInProtection) {
            shiftX.animateTo(0f)
            shiftY.animateTo(0f)
            return@LaunchedEffect
        }
        while (true) {
            delay(SHIFT_INTERVAL_MS)
            val range = SHIFT_RANGE_DP.toFloat()
            val nextX = Random.nextFloat() * 2f * range - range
            val nextY = Random.nextFloat() * 2f * range - range
            shiftX.animateTo(nextX, tween(4_000))
            shiftY.animateTo(nextY, tween(4_000))
        }
    }

    // Auto-dim: each press restarts the wait. Presses arrive on a flow rather than as state, so a
    // touch — or every move event of a drag on the seek bar — does not recompose the page; only the
    // dimmed flag itself does, and only when it flips.
    val presses = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
    var dimmed by remember { mutableStateOf(false) }
    LaunchedEffect(autoDimSeconds) {
        dimmed = false
        if (autoDimSeconds <= 0) return@LaunchedEffect
        presses.onStart { emit(Unit) }.collectLatest {
            dimmed = false
            delay(autoDimSeconds * 1_000L)
            dimmed = true
        }
    }
    val pageAlpha by animateFloatAsState(if (dimmed) DIMMED_ALPHA else 1f, tween(1_200), label = "aodDim")

    val density = LocalDensity.current
    // The song's colour (the palette's resolved target, not the colour mid-animation, so the page is
    // not redrawn on every frame of a track change); a soft neutral before it resolves.
    val songColor = state.startColor.targetValue.let { if (it.luminance() < 0.02f) Color(0xFF8E9AAF) else it }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                // Sees every touch without consuming it, so a tap both wakes a dimmed page and still
                // reaches the button under the finger.
                .pointerInput(autoDimSeconds > 0) {
                    if (autoDimSeconds <= 0) return@pointerInput
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.type == PointerEventType.Press) presses.tryEmit(Unit)
                        }
                    }
                }
                // Swallows taps so nothing in the player underneath can be hit through the page.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .offset {
                        with(density) { IntOffset(shiftX.value.dp.roundToPx(), shiftY.value.dp.roundToPx()) }
                    }.graphicsLayer { alpha = pageAlpha },
        ) {
            NightThemeBackground(theme = theme, state = state, songColor = songColor)
            Column(
                    modifier = Modifier.fillMaxSize().padding(top = if (showClock) 72.dp else 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (showClock) Spacer(Modifier.height(if (clockStyle == AOD_CLOCK_MINIMAL) 48.dp else 120.dp))
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        NightTrack(
                            state = state,
                            actions = actions,
                            compact = showClock,
                            controlsVisible = !dimmed,
                            showArtwork = theme != AOD_THEME_MINIMAL,
                        )
                    }
                }
            if (showClock) {
                NightClock(
                    style = clockStyle,
                    tint = songColor,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = !dimmed,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 12.dp),
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
                Icon(imageVector = SimpIcons.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun NightTrack(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    compact: Boolean,
    controlsVisible: Boolean,
    showArtwork: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (showArtwork) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalPlatformContext.current)
                    .data(state.screenData.thumbnailURL)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            // The player underneath is not composed while this page is open, and it is what fed the
            // artwork to the palette, so this does it instead: the glow follows a track change.
            onSuccess = { actions.onArtworkBitmap(it.result.image.toImageBitmap()) },
            modifier =
                Modifier
                    // Smaller while the clock takes the top of the page.
                    .widthIn(max = if (compact) 240.dp else 300.dp)
                    .fillMaxWidth(if (compact) 0.62f else 0.72f)
                    .aspectRatio(1f)
                    // A soft light around the cover, the one bright thing on the page.
                    .shadow(
                        elevation = 36.dp,
                        shape = NightArtworkShape,
                        ambientColor = Color.White.copy(alpha = 0.35f),
                        spotColor = Color.White.copy(alpha = 0.45f),
                    ).clip(NightArtworkShape),
        )
        Spacer(Modifier.height(if (compact) 28.dp else 36.dp))
        }
        Text(
            text = state.screenData.nowPlayingTitle,
            // Minimal has no cover, so the title carries the page.
            style = typo().titleMedium.copy(fontSize = if (showArtwork) 20.sp else 30.sp, fontWeight = FontWeight.Medium),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = state.screenData.artistName,
            style = typo().bodyMedium,
            color = NightSubtle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(if (compact) 28.dp else 40.dp))
        // Faded rather than removed while dimmed, so the page does not reflow when it wakes.
        val controlsAlpha by animateFloatAsState(if (controlsVisible) 1f else 0f, tween(600), label = "aodControls")
        Column(modifier = Modifier.graphicsLayer { alpha = controlsAlpha }) {
            NightSeekBar(state, actions)
            Spacer(Modifier.height(if (compact) 28.dp else 40.dp))
            NightTransport(state, actions)
        }
    }
}

/**
 * The page behind the track, per AOD theme. Classic and Minimal are pure black (drawn by the
 * parent); Ambient lays a soft glow of the song's colour behind the cover; Artwork fills the page
 * with the cover, blurred where the platform can blur (Android 12+) and darkened either way. All of
 * it is static — nothing here animates or redraws on its own.
 */
@Composable
private fun NightThemeBackground(
    theme: String,
    state: NowPlayingContentState,
    songColor: Color,
) {
    when (theme) {
        AOD_THEME_AMBIENT ->
            Box(
                modifier =
                    Modifier.fillMaxSize().drawBehind {
                        drawRect(
                            Brush.radialGradient(
                                listOf(songColor.copy(alpha = 0.45f), songColor.copy(alpha = 0.12f), Color.Transparent),
                                center = Offset(size.width / 2f, size.height * 0.52f),
                                radius = (size.maxDimension * 0.6f).coerceAtLeast(1f),
                            ),
                        )
                    },
            )

        AOD_THEME_ARTWORK ->
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalPlatformContext.current)
                            .data(state.screenData.thumbnailURL)
                            .crossfade(true)
                            .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(48.dp),
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.62f)))
            }

        else -> Unit
    }
}

/** Seek bar and times in their own scope, so a position tick redraws only this part. */
@Composable
private fun NightSeekBar(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    // Moves in whole seconds: the times beside it only show seconds, and at this size a once-a-second
    // step looks the same as the ten-a-second position ticks while redrawing a tenth as often.
    val barValue by remember(state) {
        derivedStateOf {
            val total = state.timelineState.total
            if (total <= 0L) {
                state.sliderValue / 100f
            } else {
                snapToSecond((total * (state.sliderValue / 100f)).roundToLong()).toFloat() / total
            }
        }
    }
    AppleMusicThinSlider(
        value = barValue,
        activeColor = Color.White,
        onValueChange = { actions.onSliderChange(it * 100f) },
        onValueChangeFinished = actions.onSliderChangeFinished,
        modifier = Modifier.fillMaxWidth(),
    )
    val elapsedMs by remember(state) {
        derivedStateOf {
            val total = state.timelineState.total
            snapToSecond(if (total > 0L) (total * (state.sliderValue / 100f)).roundToLong() else state.timelineState.current)
        }
    }
    val totalMs by remember(state) { derivedStateOf { snapToSecond(state.timelineState.total) } }
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(text = formatDuration(elapsedMs), style = typo().bodySmall, color = NightSubtle, modifier = Modifier.weight(1f))
        Text(text = formatDuration(totalMs), style = typo().bodySmall, color = NightSubtle)
    }
}

@Composable
private fun NightTransport(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    val controls = state.controllerState
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { if (controls.isPreviousAvailable) actions.onUIEvent(UIEvent.Previous) },
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                imageVector = SimpIcons.FastRewind,
                contentDescription = "Previous",
                tint = if (controls.isPreviousAvailable) Color.White else NightSubtle,
                modifier = Modifier.size(32.dp),
            )
        }
        Box(
            modifier =
                Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { actions.onUIEvent(UIEvent.PlayPause) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (controls.isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                contentDescription = if (controls.isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(34.dp),
            )
        }
        IconButton(
            onClick = { if (controls.isNextAvailable) actions.onUIEvent(UIEvent.Next) },
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                imageVector = SimpIcons.FastForward,
                contentDescription = "Next",
                tint = if (controls.isNextAvailable) Color.White else NightSubtle,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

/** The current local time, updated on each minute boundary rather than polled. */
@OptIn(ExperimentalTime::class)
@Composable
private fun rememberMinuteClock(): LocalDateTime {
    fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    var time by remember { mutableStateOf(now()) }
    LaunchedEffect(Unit) {
        while (true) {
            val current = now()
            time = current
            // Sleep to the start of the next minute, so the display changes on the minute.
            val msIntoMinute = current.second * 1_000L + current.nanosecond / 1_000_000L
            delay(60_000L - msIntoMinute + 50L)
        }
    }
    return time
}

@Composable
private fun NightClock(
    style: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val time = rememberMinuteClock()
    val is24Hour = rememberIs24HourClock()
    val hour = if (is24Hour) time.hour else ((time.hour + 11) % 12) + 1
    val clockText = (if (is24Hour) hour.toString().padStart(2, '0') else hour.toString()) + ":" + time.minute.toString().padStart(2, '0')
    val dateText = "${dayName(time.dayOfWeek)}, ${time.day} ${monthShortName(time.month)}"

    when (style) {
        AOD_CLOCK_MINIMAL ->
            Text(
                text = "$clockText  ·  $dateText",
                style = typo().titleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.85f),
                modifier = modifier,
            )

        AOD_CLOCK_THIN, AOD_CLOCK_BOLD ->
            Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = clockText,
                    style =
                        typo().titleLarge.copy(
                            fontSize = if (style == AOD_CLOCK_THIN) 84.sp else 72.sp,
                            lineHeight = if (style == AOD_CLOCK_THIN) 88.sp else 76.sp,
                            fontWeight = if (style == AOD_CLOCK_THIN) FontWeight.ExtraLight else FontWeight.Bold,
                        ),
                    color = Color.White,
                )
                Text(text = dateText, style = typo().bodyMedium, color = NightSubtle)
            }

        else ->
            // Liquid glass, the default: the digits themselves are the glass — the same glass as the
            // nav bar and the player buttons, cut to the shape of the time.
            Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                GlassText(
                    text = clockText,
                    style = typo().titleLarge.copy(fontSize = 96.sp, lineHeight = 100.sp, fontWeight = FontWeight.Black),
                    tint = tint,
                )
                Text(text = dateText, style = typo().bodyMedium, color = Color.White.copy(alpha = 0.7f))
            }
    }
}

/**
 * [text] drawn as liquid glass, glyph by glyph — no box behind it. A glass surface over a black page
 * has nothing to refract and renders as a dark slab, so the glass is built from its parts instead,
 * every layer drawn with the text as its own mask:
 * - a soft glow of [tint] spilling out around the glyphs, the light the glass bends;
 * - a translucent body, clear at the top and carrying the tint lower down, like thick glass;
 * - a diagonal specular streak across all the digits at once, as one sheet of glass would catch it;
 * - a rim lit from above and faintly from below, which is what reads as an edge on a dark page.
 * One draw pass, redrawn only when the minute or the song colour changes.
 */
@Composable
private fun GlassText(
    text: String,
    style: TextStyle,
    tint: Color,
) {
    val measurer = rememberTextMeasurer()
    val layout = remember(text, style) { measurer.measure(text, style) }
    val density = LocalDensity.current
    val boxSize = with(density) { DpSize(layout.size.width.toDp(), layout.size.height.toDp()) }
    val rimWidth = with(density) { 1.5.dp.toPx() }
    val glowRadius = with(density) { 26.dp.toPx() }
    Canvas(modifier = Modifier.size(boxSize)) {
        val w = size.width
        val h = size.height
        // Glow: the shadow of a barely-there fill, so the light follows the glyph outlines.
        drawText(
            layout,
            color = tint.copy(alpha = 0.18f),
            shadow = Shadow(color = tint.copy(alpha = 0.75f), blurRadius = glowRadius),
        )
        // Body.
        drawText(
            layout,
            brush =
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.34f),
                    0.5f to lerp(Color.White, tint, 0.55f).copy(alpha = 0.22f),
                    1f to tint.copy(alpha = 0.30f),
                    startY = 0f,
                    endY = h,
                ),
        )
        // Specular streak.
        drawText(
            layout,
            brush =
                Brush.linearGradient(
                    0f to Color.Transparent,
                    0.38f to Color.Transparent,
                    0.46f to Color.White.copy(alpha = 0.40f),
                    0.52f to Color.Transparent,
                    1f to Color.Transparent,
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                ),
        )
        // Rim.
        drawText(
            layout,
            brush =
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.95f),
                    0.45f to Color.White.copy(alpha = 0.28f),
                    1f to lerp(Color.White, tint, 0.4f).copy(alpha = 0.65f),
                    startY = 0f,
                    endY = h,
                ),
            drawStyle = Stroke(width = rimWidth),
        )
    }
}

private fun dayName(day: DayOfWeek): String =
    when (day) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }
