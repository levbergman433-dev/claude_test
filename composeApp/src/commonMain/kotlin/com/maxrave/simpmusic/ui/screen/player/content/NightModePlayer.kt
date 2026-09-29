package com.maxrave.simpmusic.ui.screen.player.content

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.simpmusic.expect.ui.ImmersiveSystemBars
import com.maxrave.simpmusic.expect.ui.PlatformBackHandler
import com.maxrave.simpmusic.extension.KeepScreenOn
import com.maxrave.simpmusic.extension.formatDuration
import com.maxrave.simpmusic.ui.icon.Close
import com.maxrave.simpmusic.ui.icon.FastForward
import com.maxrave.simpmusic.ui.icon.FastRewind
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.screen.player.content.applemusic.AppleMusicThinSlider
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.UIEvent
import kotlin.math.roundToLong

private val NightArtworkShape = RoundedCornerShape(10.dp)
private val NightSubtle = Color.White.copy(alpha = 0.55f)

/**
 * The night-mode player: a pure black page with only the artwork, the track, a seek bar and the
 * three transport buttons — for a phone lying beside the bed or on a stand. Black pixels are off on
 * an OLED screen, the system bars are hidden, and the screen is kept awake while it is open.
 *
 * It sits over the whole player, whichever style is chosen, and closes with the ✕ or back.
 */
@Composable
internal fun NightModePlayer(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    onClose: () -> Unit,
) {
    KeepScreenOn()
    ImmersiveSystemBars()
    PlatformBackHandler(enabled = true, onBack = onClose)

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                // Swallows taps so nothing in the player underneath can be hit through the page.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
    ) {
        IconButton(
            onClick = onClose,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 28.dp, end = 16.dp)
                    .size(48.dp),
        ) {
            Icon(imageVector = SimpIcons.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.8f))
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val artwork = state.screenData.thumbnailURL
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(artwork)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .widthIn(max = 300.dp)
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1f)
                        // A soft light around the cover, the one bright thing on the page.
                        .shadow(
                            elevation = 36.dp,
                            shape = NightArtworkShape,
                            ambientColor = Color.White.copy(alpha = 0.35f),
                            spotColor = Color.White.copy(alpha = 0.45f),
                        ).clip(NightArtworkShape),
            )
            Spacer(Modifier.height(36.dp))
            Text(
                text = state.screenData.nowPlayingTitle,
                style = typo().titleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium),
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
            Spacer(Modifier.height(40.dp))
            NightSeekBar(state, actions)
            Spacer(Modifier.height(40.dp))
            NightTransport(state, actions)
        }
    }
}

/** Seek bar and times in their own scope, so a position tick redraws only this part. */
@Composable
private fun NightSeekBar(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    AppleMusicThinSlider(
        value = state.sliderValue / 100f,
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
