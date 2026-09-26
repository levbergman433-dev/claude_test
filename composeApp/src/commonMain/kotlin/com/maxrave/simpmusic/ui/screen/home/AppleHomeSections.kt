package com.maxrave.simpmusic.ui.screen.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.domain.data.model.home.Content
import com.maxrave.domain.data.model.home.HomeItem
import com.maxrave.domain.data.model.home.chart.Chart
import com.maxrave.simpmusic.ui.component.AppleAlbumCard
import com.maxrave.simpmusic.ui.component.AppleEdge
import com.maxrave.simpmusic.ui.component.AppleTileGap
import com.maxrave.simpmusic.ui.component.AppleTileRow
import com.maxrave.simpmusic.ui.component.appleSecondaryTextColor
import com.maxrave.simpmusic.ui.component.appleSeparatorColor
import com.maxrave.simpmusic.ui.component.AppleShelfHeader
import com.maxrave.simpmusic.ui.component.AppleSongGrid
import com.maxrave.simpmusic.ui.component.homeContentClick
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.HomeViewModel
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.best_new_songs
import simpmusic.composeapp.generated.resources.new_release
import simpmusic.composeapp.generated.resources.new_this_week
import simpmusic.composeapp.generated.resources.top_artists

private const val FEATURED_COUNT = 5

/**
 * The top of Home in the Apple Music layout, structured like Apple Music's "New" tab:
 * a featured carousel, "Best New Songs" as a four-row song list, "New This Week" as square tiles,
 * then the charts and top artists. Everything comes from data Home already loads — YouTube Music's
 * new releases (albums/singles and new music videos) and its charts.
 */
@Composable
fun AppleNewTabSections(
    newRelease: List<HomeItem>,
    chart: Chart?,
    navController: NavController,
    homeViewModel: HomeViewModel,
    onMore: (Content) -> Unit,
) {
    // New-release shelves come as one of albums/singles and one of new music videos.
    val songShelf = newRelease.firstOrNull { shelf -> shelf.contents.filterNotNull().let { it.isNotEmpty() && it.all { c -> !c.videoId.isNullOrEmpty() } } }
    val albums = newRelease.filter { it != songShelf }.flatMap { it.contents.filterNotNull() }
    val featured = albums.take(FEATURED_COUNT)
    val newThisWeek = albums.drop(FEATURED_COUNT)

    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        if (featured.isNotEmpty()) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                // One card per screen, with a sliver of the next one past the right edge.
                val cardWidth = maxWidth - AppleEdge - AppleTileGap - 16.dp
                val state = rememberLazyListState()
                LazyRow(
                    state = state,
                    contentPadding = PaddingValues(horizontal = AppleEdge),
                    horizontalArrangement = Arrangement.spacedBy(AppleTileGap),
                    flingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(state, SnapPosition.Start)),
                ) {
                    items(featured) { item ->
                        AppleFeaturedCard(
                            label = stringResource(Res.string.new_release).uppercase(),
                            data = item,
                            width = cardWidth,
                            onClick = { homeContentClick(item, navController, homeViewModel) },
                        )
                    }
                }
            }
        }
        songShelf?.let { shelf ->
            Column {
                AppleShelfHeader(title = stringResource(Res.string.best_new_songs), modifier = Modifier.padding(bottom = 8.dp))
                AppleSongGrid(
                    items = shelf.contents.filterNotNull(),
                    onClick = { homeContentClick(it, navController, homeViewModel) },
                    onMore = onMore,
                )
            }
        }
        if (newThisWeek.isNotEmpty()) {
            Column {
                AppleShelfHeader(title = stringResource(Res.string.new_this_week), modifier = Modifier.padding(bottom = 8.dp))
                AppleTileRow(newThisWeek) { item, size ->
                    AppleAlbumCard(
                        title = item.title,
                        subtitle = item.artists?.joinToString(", ") { it.name }?.takeIf { it.isNotBlank() } ?: item.description,
                        artwork = item.thumbnails.lastOrNull()?.url,
                        size = size,
                        onClick = { homeContentClick(item, navController, homeViewModel) },
                    )
                }
            }
        }
        chart?.listChartItem?.filter { it.playlists.isNotEmpty() }?.forEach { chartShelf ->
            Column {
                AppleShelfHeader(title = chartShelf.title, modifier = Modifier.padding(bottom = 8.dp))
                AppleTileRow(chartShelf.playlists) { playlist, size ->
                    AppleAlbumCard(
                        title = playlist.title,
                        subtitle = playlist.author,
                        artwork = playlist.thumbnails.lastOrNull()?.url,
                        size = size,
                        onClick = {
                            navController.navigate(
                                PlaylistDestination(playlistId = playlist.id, isYourYouTubePlaylist = false),
                            )
                        },
                    )
                }
            }
        }
        val artists = chart?.artists?.itemArtists.orEmpty()
        if (artists.isNotEmpty()) {
            Column {
                AppleShelfHeader(title = stringResource(Res.string.top_artists), modifier = Modifier.padding(bottom = 8.dp))
                AppleTileRow(artists) { artist, size ->
                    AppleAlbumCard(
                        title = artist.title,
                        subtitle = null,
                        artwork = artist.thumbnails.lastOrNull()?.url,
                        size = size * 0.8f,
                        circle = true,
                        onClick = { navController.navigate(ArtistDestination(channelId = artist.browseId)) },
                    )
                }
            }
        }
    }
}

/**
 * Apple Music's featured card: a small uppercase label, the title and a secondary line ABOVE a
 * wide card. The card shows the square cover centred on a blurred, dimmed wash of itself, so an
 * album cover fills a wide card without being cropped.
 */
@Composable
private fun AppleFeaturedCard(
    label: String,
    data: Content,
    width: Dp,
    onClick: () -> Unit,
) {
    val artwork = data.thumbnails.lastOrNull()?.url
    val subtitle = data.artists?.joinToString(", ") { it.name }?.takeIf { it.isNotBlank() } ?: data.description
    Column(
        modifier =
            Modifier
                .width(width)
                .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            style =
                typo().bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp,
                    color = appleSecondaryTextColor(),
                ),
            maxLines = 1,
        )
        Text(
            text = data.title,
            style =
                typo().titleMedium.copy(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = typo().bodyMedium.copy(fontSize = 16.sp, color = appleSecondaryTextColor()),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            modifier =
                Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(0.5.dp, appleSeparatorColor(), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            val request =
                ImageRequest
                    .Builder(LocalPlatformContext.current)
                    .data(artwork)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .diskCacheKey(artwork)
                    .crossfade(300)
                    .build()
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .matchParentSize()
                        .blur(28.dp)
                        .drawWithContent {
                            drawContent()
                            drawRect(Color.Black.copy(alpha = 0.35f))
                        },
            )
            AsyncImage(
                model = request,
                contentDescription = null,
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxHeight(0.78f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(6.dp)),
            )
        }
    }
}
