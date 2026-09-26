package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Color
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
                .padding(horizontal = AppleEdge - 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .padding(horizontal = 4.dp)
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (onClick != null) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = SimpIcons.ArrowForwardIos,
                    contentDescription = null,
                    tint = appleSecondaryTextColor(),
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

/**
 * Apple's separator: an OPAQUE-looking hairline (#38383A on black), not a translucent tint of the
 * accent — a faint, seed-coloured line is one of the things that made shelves read as cheap.
 */
@Composable
fun appleSeparatorColor(): Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.21f)

/**
 * Apple's secondary label: a neutral grey (#8E8E93 on black). The theme's onSurfaceVariant is
 * tinted by the seed colour, which is why artist names came out pink or lavender.
 */
@Composable
fun appleSecondaryTextColor(): Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.56f)

/** Apple Music's page title ("Home", "Library"): regular weight, not a heavy large title. */
@Composable
fun applePageTitleStyle(): TextStyle =
    typo().titleLarge.copy(
        fontSize = 25.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onBackground,
    )

/** The hairline Apple Music draws under its top bar at all times. */
@Composable
fun AppleTopBarSeparator() {
    HorizontalDivider(thickness = 0.5.dp, color = appleSeparatorColor())
}

/** Page edge inset for Apple Music shelves; rows scroll under it to the screen edge. */
val AppleEdge = 20.dp

/** Gap between tiles in an Apple Music shelf. */
val AppleTileGap = 12.dp

/**
 * Apple Music's tile size: two whole tiles and a slice of the third across the screen, with the
 * slice (~10% of the width) telling the user the row scrolls. Capped so wide windows show more
 * tiles instead of giant ones.
 */
fun appleTileSize(width: androidx.compose.ui.unit.Dp): androidx.compose.ui.unit.Dp =
    ((width * 0.9f - AppleEdge - AppleTileGap * 2) / 2).coerceIn(120.dp, 200.dp)

/**
 * A horizontally scrolling Apple Music shelf row. It spans the full screen width and insets its
 * content by [AppleEdge], so tiles slide off the screen edge instead of being cut at the page
 * padding, and it snaps a tile to the leading edge after a fling.
 */
@Composable
fun <T> AppleTileRow(
    items: List<T>,
    key: ((T) -> Any)? = null,
    tile: @Composable (item: T, size: androidx.compose.ui.unit.Dp) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val size = appleTileSize(maxWidth)
        val state = rememberLazyListState()
        LazyRow(
            state = state,
            contentPadding = PaddingValues(horizontal = AppleEdge),
            horizontalArrangement = Arrangement.spacedBy(AppleTileGap),
            flingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(state, SnapPosition.Start)),
        ) {
            items(items, key = key) { tile(it, size) }
        }
    }
}

private val AppleSongRowHeight = 60.dp

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
    val columnWidth = maxWidth * 0.9f - AppleEdge - AppleTileGap
    LazyHorizontalGrid(
        rows = GridCells.Fixed(4),
        state = state,
        contentPadding = PaddingValues(horizontal = AppleEdge),
        horizontalArrangement = Arrangement.spacedBy(AppleTileGap),
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
                        .size(48.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .border(0.5.dp, appleSeparatorColor(), RoundedCornerShape(5.dp)),
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style =
                            typo().bodyLarge.copy(
                                fontSize = 15.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
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
                        style =
                            typo().bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = appleSecondaryTextColor(),
                            ),
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
                        // Starts under the title, runs to the column's end (under the ⋮ too).
                        .padding(start = 62.dp),
                thickness = 0.5.dp,
                color = appleSeparatorColor(),
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
    size: androidx.compose.ui.unit.Dp,
    circle: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val shape = if (circle) CircleShape else RoundedCornerShape(10.dp)
    Column(
        modifier =
            Modifier
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
                    .clip(shape)
                    // Apple outlines artwork with a hairline so dark covers keep their edge on a
                    // black page.
                    .border(0.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f), shape),
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = title,
            style =
                typo().bodyLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style =
                    typo().bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = appleSecondaryTextColor(),
                    ),
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
        AppleTileRow(songs, key = { it.videoId }) { song, size ->
            AppleAlbumCard(
                title = song.title,
                subtitle = song.artistName?.joinToString(", "),
                artwork = song.thumbnails,
                size = size,
                onClick = { onSongClick(song) },
                onLongClick = { onSongLongClick(song) },
            )
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
                tint = appleSecondaryTextColor(),
                modifier = Modifier.size(14.dp),
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 60.dp),
                thickness = 0.5.dp,
                color = appleSeparatorColor(),
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
