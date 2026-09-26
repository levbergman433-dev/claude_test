package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.simpmusic.ui.icon.CheckCircle
import com.maxrave.simpmusic.ui.icon.Crown
import com.maxrave.simpmusic.ui.icon.Diamond
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.Flame
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.Star
import com.maxrave.simpmusic.ui.icon.Verified
import com.maxrave.simpmusic.ui.theme.LocalUseInter
import com.maxrave.simpmusic.ui.theme.fontFamily
import org.jetbrains.compose.resources.Font
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.dancing_script
import simpmusic.composeapp.generated.resources.poppins_medium

/** The icon shown after the name. */
enum class BadgeIcon {
    VERIFIED,
    CHECK,
    STAR,
    HEART,
    CROWN,
    DIAMOND,
    FLAME,
    NONE,
    ;

    val vector: ImageVector?
        get() =
            when (this) {
                VERIFIED -> SimpIcons.Verified
                CHECK -> SimpIcons.CheckCircle
                STAR -> SimpIcons.Star
                HEART -> SimpIcons.Favorite
                CROWN -> SimpIcons.Crown
                DIAMOND -> SimpIcons.Diamond
                FLAME -> SimpIcons.Flame
                NONE -> null
            }
}

/** The typeface of the name. */
enum class BadgeFont {
    SCRIPT,
    APP,
    POPPINS,
}

/**
 * A personal badge for the top of Home: a round picture with the name overlapping its lower
 * right corner, and an optional icon after it — in the style of a hand-made home screen. Purely
 * decorative; nothing here is clickable.
 */
@Composable
fun ProfileBadge(
    name: String,
    avatar: String?,
    nameColor: Color,
    font: BadgeFont,
    icon: BadgeIcon,
    iconColor: Color,
    modifier: Modifier = Modifier,
) {
    val family =
        when (font) {
            BadgeFont.SCRIPT -> FontFamily(Font(Res.font.dancing_script, FontWeight.Normal, FontStyle.Normal))
            BadgeFont.POPPINS -> FontFamily(Font(Res.font.poppins_medium, FontWeight.Normal, FontStyle.Normal))
            BadgeFont.APP -> if (LocalUseInter.current) fontFamily() else FontFamily(Font(Res.font.poppins_medium, FontWeight.Normal, FontStyle.Normal))
        }
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalPlatformContext.current)
                    .data(avatar)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(300)
                    .build(),
            contentDescription = null,
            placeholder = rememberHolderPainter(),
            error = rememberHolderPainter(),
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color.Black.copy(alpha = 0.6f), CircleShape),
        )
        // The name tucks under the picture's corner, like the reference home screen.
        Text(
            text = name,
            style =
                TextStyle(
                    fontFamily = family,
                    fontSize = if (font == BadgeFont.SCRIPT) 22.sp else 17.sp,
                    fontWeight = if (font == BadgeFont.SCRIPT) FontWeight.Normal else FontWeight.SemiBold,
                    color = nameColor,
                    // A soft outline so the name reads over the picture and over any page colour.
                    shadow = Shadow(Color.Black.copy(alpha = 0.7f), Offset(0f, 1.5f), 4f),
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(x = (-10).dp, y = 2.dp),
        )
        icon.vector?.let { vector ->
            Spacer(Modifier.width(0.dp))
            Icon(
                imageVector = vector,
                contentDescription = null,
                tint = iconColor,
                modifier =
                    Modifier
                        .offset(x = (-6).dp, y = (-1).dp)
                        .size(17.dp),
            )
        }
    }
}
