package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.compose.LocalPlatformContext
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.home.Content
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.ArrowForwardIos
import com.maxrave.simpmusic.ui.icon.MoreVert
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo

/*
 * Building blocks for the "Apple Music layout" setting (LocalAppleLayout). They reproduce the
 * STRUCTURE of Apple Music's pages rather than its colours, which keep coming from the app theme:
 *  - Home: shelves are headed by a bold title with a chevron, and the first shelf is a carousel of
 *    large "Top Picks" cards instead of small tiles;
 *  - Library: the landing page is a list of categories (icon · name · chevron) above "Recently
 *    Added", instead of a chip row and coloured tiles.
 */

/**
 * Apple Music shelf header: a bold title with the chevron sitting right after it (not pushed to
 * the edge), shown only when the shelf actually opens somewhere. [caption] is kept for callers
 * that need a second line, but Apple's own shelves have none.
 */
@Composable
fun AppleShelfHeader(
    title: String,
    caption: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(vertical = 4.dp),
    ) {
        if (!caption.isNullOrBlank()) {
            Text(
                text = caption,
                style = typo().bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style =
                    typo().titleLarge.copy(
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (onClick != null) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = SimpIcons.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private val AppleSongRowHeight = 68.dp

/**
 * Apple Music's song shelf ("Best New Songs"): songs laid out as a list, four rows per column,
 * columns paged horizontally with the next one peeking in at the edge. Each row is a small
 * rounded cover, title, artist and a ⋮ button, separated by hairlines inset past the cover.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleSongGrid(
    items: List<Content>,
    onClick: (Content) -> Unit,
    onMore: (Content) -> Unit,
) {
    val state = rememberLazyGridState()
    BoxWithConstraints(Modifier.fillMaxWidth()) {
    // A column fills most of the width so the next one peeks in, as in Apple Music.
    val columnWidth = maxWidth * 0.86f
    LazyHorizontalGrid(
        rows = GridCells.Fixed(4),
        state = state,
        flingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = state, snapPosition = SnapPosition.Start)),
        modifier = Modifier.height(AppleSongRowHeight * 4),
    ) {
        itemsIndexed(items, key = { index, item -> "${item.videoId}-$index" }) { index, item ->
            AppleSongRow(
                title = item.title,
                subtitle = item.artists?.joinToString(", ") { it.name }.orEmpty(),
                artwork = item.thumbnails.lastOrNull()?.url,
                isExplicit = item.isExplicit == true,
                showDivider = index % 4 != 3 && index != items.lastIndex,
                width = columnWidth,
                onClick = { onClick(item) },
                onMore = { onMore(item) },
            )
        }
    }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleSongRow(
    title: String,
    subtitle: String,
    artwork: String?,
    isExplicit: Boolean,
    showDivider: Boolean,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    onMore: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .width(width)
                .height(AppleSongRowHeight)
                .combinedClickable(onClick = onClick, onLongClick = onMore),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(artwork)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(artwork)
                        .crossfade(300)
                        .build(),
                contentDescription = null,
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(6.dp)),
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = typo().bodyLarge.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isExplicit) {
                        Spacer(Modifier.width(4.dp))
                        ExplicitBadge(modifier = Modifier.size(16.dp))
                    }
                }
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = typo().bodyMedium.copy(fontSize = 14.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onMore) {
                Icon(
                    imageVector = SimpIcons.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 66.dp, end = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
    }
}

/**
 * Apple Music's album/playlist tile ("New This Week"): a large square cover with a subtle rounded
 * corner, the title and a secondary line beneath it. Artists are drawn with a circular photo.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleAlbumCard(
    title: String,
    subtitle: String?,
    artwork: String?,
    circle: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 170.dp,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Column(
        modifier =
            Modifier
                .padding(end = 14.dp)
                .width(size)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        horizontalAlignment = if (circle) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalPlatformContext.current)
                    .data(artwork)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .diskCacheKey(artwork)
                    .crossfade(300)
                    .build(),
            contentDescription = null,
            placeholder = rememberHolderPainter(),
            error = rememberHolderPainter(),
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(size)
                    .clip(if (circle) CircleShape else RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            style = typo().bodyLarge.copy(fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = typo().bodyMedium.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Apple Music's "Recently Played" shelf, filled from the local play history: the same square
 * tiles as an album shelf, and a chevron header that opens the full history.
 */
@Composable
fun AppleRecentlyPlayedShelf(
    title: String,
    songs: List<SongEntity>,
    onSongClick: (SongEntity) -> Unit,
    onSongLongClick: (SongEntity) -> Unit,
    onSeeAll: () -> Unit,
) {
    Column {
        AppleShelfHeader(title = title, onClick = onSeeAll, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow {
            items(songs, key = { it.videoId }) { song ->
                AppleAlbumCard(
                    title = song.title,
                    subtitle = song.artistName?.joinToString(", "),
                    artwork = song.thumbnails,
                    onClick = { onSongClick(song) },
                    onLongClick = { onSongLongClick(song) },
                )
            }
        }
    }
}

/** One Apple Music Library row: accent icon, name, chevron, with an inset hairline below. */
@Composable
fun AppleLibraryRow(
    title: String,
    icon: ImageVector,
    showDivider: Boolean = true,
    onClick: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 20.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                style =
                    typo().bodyLarge.copy(
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = SimpIcons.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp),
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 60.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
    }
}

/** "‹ Library"-style back row shown above a Library sub-page when the chip row is hidden. */
@Composable
fun AppleBackRow(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = SimpIcons.ArrowBackIosNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            style = typo().bodyLarge.copy(fontSize = 17.sp, color = MaterialTheme.colorScheme.primary),
        )
    }
}
