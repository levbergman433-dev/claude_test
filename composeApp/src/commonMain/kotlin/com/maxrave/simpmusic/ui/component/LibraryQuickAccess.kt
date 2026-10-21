package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.common.LibraryChipType
import com.maxrave.domain.data.entities.AlbumEntity
import com.maxrave.domain.data.entities.ArtistEntity
import com.maxrave.domain.data.entities.PlaylistEntity
import com.maxrave.domain.data.entities.PodcastsEntity
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.library.LibraryCollectionPreview
import com.maxrave.domain.data.model.library.LibraryOverview
import com.maxrave.domain.data.type.RecentlyType
import com.maxrave.simpmusic.ui.component.taste.libraryCardColor
import com.maxrave.simpmusic.ui.icon.AutoGraph
import com.maxrave.simpmusic.ui.icon.Download
import com.maxrave.simpmusic.ui.icon.DownloadForOffline
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.Insights
import com.maxrave.simpmusic.ui.icon.LibraryMusic
import com.maxrave.simpmusic.ui.icon.PeopleAlt
import com.maxrave.simpmusic.ui.icon.QueueMusic
import com.maxrave.simpmusic.ui.icon.RssFeed
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.Star
import com.maxrave.simpmusic.ui.icon.TrendingUp
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.downloaded
import simpmusic.composeapp.generated.resources.downloaded_playlists
import simpmusic.composeapp.generated.resources.favorite
import simpmusic.composeapp.generated.resources.favorite_playlists
import simpmusic.composeapp.generated.resources.favorite_podcasts
import simpmusic.composeapp.generated.resources.followed
import simpmusic.composeapp.generated.resources.library_artist_count
import simpmusic.composeapp.generated.resources.liked_songs_count
import simpmusic.composeapp.generated.resources.most_played
import simpmusic.composeapp.generated.resources.simpmusic_charts
import simpmusic.composeapp.generated.resources.wrapped
import simpmusic.composeapp.generated.resources.your_playlists
import simpmusic.composeapp.generated.resources.your_youtube_playlists

/*
 * The top of the Your library tab, built to fit on one screen with the history right under it.
 * It replaced eleven full-width category rows, which pushed "Recently Added" two screens down: the
 * four collections are now a 2 × 2 grid of short tiles, and the rest of the categories a wrapped row
 * of pills, so every way into the library is visible at once without scrolling sideways either.
 */

/**
 * Favorite, Downloaded, Most played and Followed as short tiles, each led by the artwork of its
 * newest item (a round one for the artists). An empty collection, or one not read yet, shows its
 * icon instead, so the tile never looks broken.
 */
@Composable
fun LibraryShortcutGrid(
    navController: NavController,
    overview: LibraryOverview?,
    modifier: Modifier = Modifier,
) {
    val open = { type: LibraryDynamicPlaylistType ->
        navController.navigate(LibraryDynamicPlaylistDestination(type = type.toStringParams()))
    }
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = AppleEdge - 4.dp),
        verticalArrangement = Arrangement.spacedBy(TILE_GAP),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(TILE_GAP)) {
            ShortcutTile(
                title = stringResource(Res.string.favorite),
                subtitle = songCount(overview?.favorite),
                preview = overview?.favorite,
                icon = SimpIcons.Favorite,
                round = false,
                modifier = Modifier.weight(1f),
            ) { open(LibraryDynamicPlaylistType.Favorite) }
            ShortcutTile(
                title = stringResource(Res.string.downloaded),
                subtitle = songCount(overview?.downloaded),
                preview = overview?.downloaded,
                icon = SimpIcons.DownloadForOffline,
                round = false,
                modifier = Modifier.weight(1f),
            ) { open(LibraryDynamicPlaylistType.Downloaded) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(TILE_GAP)) {
            ShortcutTile(
                title = stringResource(Res.string.most_played),
                subtitle = songCount(overview?.mostPlayed),
                preview = overview?.mostPlayed,
                icon = SimpIcons.TrendingUp,
                round = false,
                modifier = Modifier.weight(1f),
            ) { open(LibraryDynamicPlaylistType.MostPlayed) }
            ShortcutTile(
                title = stringResource(Res.string.followed),
                subtitle = overview?.followed?.count?.let { pluralStringResource(Res.plurals.library_artist_count, it, it) },
                preview = overview?.followed,
                icon = SimpIcons.PeopleAlt,
                round = true,
                modifier = Modifier.weight(1f),
            ) { open(LibraryDynamicPlaylistType.Followed) }
        }
    }
}

@Composable
private fun ShortcutTile(
    title: String,
    subtitle: String?,
    preview: LibraryCollectionPreview?,
    icon: ImageVector,
    round: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val artwork = preview?.thumbnails?.firstOrNull()
    Row(
        modifier =
            modifier
                .height(TILE_HEIGHT)
                .clip(TileShape)
                .background(libraryCardColor())
                .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(TILE_HEIGHT), contentAlignment = Alignment.Center) {
            if (artwork != null) {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalPlatformContext.current)
                            .data(artwork)
                            .crossfade(true)
                            .build(),
                    placeholder = rememberHolderPainter(),
                    error = rememberHolderPainter(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        if (round) {
                            Modifier.padding(8.dp).fillMaxSize().clip(CircleShape)
                        } else {
                            Modifier.fillMaxSize()
                        },
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp).accentTint(),
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = title,
                style = typo().titleSmall.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = typo().bodySmall.copy(fontSize = 12.sp, color = appleSecondaryTextColor()),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Every other way into the library as a pill: your playlists, YouTube's (signed in only), saved
 * and downloaded playlists, podcasts, charts and Wrapped (local tracking only). Wrapped onto as
 * many lines as it takes rather than scrolled sideways, so none of them hides off the edge.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryCollectionPills(
    showYouTube: Boolean,
    showWrapped: Boolean,
    onOpenCategory: (LibraryChipType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pills =
        buildList {
            add(Triple(Res.string.your_playlists, SimpIcons.QueueMusic, LibraryChipType.LOCAL_PLAYLIST))
            if (showYouTube) add(Triple(Res.string.your_youtube_playlists, SimpIcons.LibraryMusic, LibraryChipType.YOUTUBE_MUSIC_PLAYLIST))
            add(Triple(Res.string.favorite_playlists, SimpIcons.Star, LibraryChipType.FAVORITE_PLAYLIST))
            add(Triple(Res.string.downloaded_playlists, SimpIcons.Download, LibraryChipType.DOWNLOADED_PLAYLIST))
            add(Triple(Res.string.favorite_podcasts, SimpIcons.RssFeed, LibraryChipType.FAVORITE_PODCAST))
            add(Triple(Res.string.simpmusic_charts, SimpIcons.AutoGraph, LibraryChipType.CHART))
            if (showWrapped) add(Triple(Res.string.wrapped, SimpIcons.Insights, LibraryChipType.WRAPPED))
        }
    FlowRow(
        modifier = modifier.fillMaxWidth().padding(horizontal = AppleEdge - 4.dp),
        horizontalArrangement = Arrangement.spacedBy(PILL_GAP),
        verticalArrangement = Arrangement.spacedBy(PILL_GAP),
    ) {
        pills.forEach { (label, icon, type) ->
            Row(
                modifier =
                    Modifier
                        .height(PILL_HEIGHT)
                        .clip(CircleShape)
                        .background(libraryCardColor())
                        .clickable { onOpenCategory(type) }
                        .padding(start = 12.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp).accentTint(),
                )
                Text(
                    text = stringResource(label),
                    style = typo().titleSmall.copy(color = MaterialTheme.colorScheme.onBackground),
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * A key for one history entry that stays the same across reloads (the list is rebuilt on every
 * track change), and is unique within it: each kind has its own id, so the kind goes in the key too.
 */
fun RecentlyType.recentlyKey(): String =
    when (this) {
        is SongEntity -> "song:$videoId"
        is ArtistEntity -> "artist:$channelId"
        is AlbumEntity -> "album:$browseId"
        is PlaylistEntity -> "playlist:$id"
        is PodcastsEntity -> "podcast:$podcastId"
        else -> "other:${hashCode()}"
    }

private val TILE_HEIGHT = 56.dp
private val TILE_GAP = 8.dp
private val TileShape = RoundedCornerShape(10.dp)
private val PILL_HEIGHT = 36.dp
private val PILL_GAP = 8.dp

// "24 songs" under a tile, or nothing while the collection has not been read.
@Composable
private fun songCount(preview: LibraryCollectionPreview?): String? =
    preview?.count?.let { pluralStringResource(Res.plurals.liked_songs_count, it, it) }