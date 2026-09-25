package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.maxrave.domain.data.model.home.Content
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.ArrowForwardIos
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

/** Apple Music shelf header: optional small caption, then a bold title and — when tappable — a chevron. */
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
                        fontSize = 21.sp,
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

/**
 * A large "Top Picks" card: square artwork with the title and subtitle on a tinted panel below,
 * the whole card one rounded surface — Apple Music's hero shelf, sized to show one and a half cards
 * on a phone so the row reads as swipeable.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleHeroCard(
    data: Content,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    width: androidx.compose.ui.unit.Dp = 260.dp,
) {
    val subtitle =
        data.artists?.joinToString(", ") { it.name }?.takeIf { it.isNotBlank() }
            ?: data.description
            ?: data.album?.name
    val artwork = data.thumbnails.lastOrNull()?.url
    Column(
        modifier =
            Modifier
                .padding(end = 12.dp)
                .width(width)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
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
                    .fillMaxWidth()
                    .aspectRatio(1f),
        )
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = data.title,
                style =
                    typo().titleSmall.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = typo().bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
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
