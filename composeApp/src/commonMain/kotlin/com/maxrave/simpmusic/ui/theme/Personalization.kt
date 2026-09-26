package com.maxrave.simpmusic.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb

/**
 * Keys for the personalisation settings (profile badge, custom page and accent fills). They are
 * plain string preferences read through DataStoreManager.getString, so no schema change is needed.
 */
object PersonalizationKeys {
    // Custom page fill ("Theme" > Custom…)
    const val PAGE_FILL = "page_fill"

    // Custom accent fill ("Theme color" > Gradient)
    const val ACCENT_FILL = "accent_fill"

    const val BADGE_ENABLED = "profile_badge_enabled"
    const val BADGE_NAME = "profile_badge_name"
    const val BADGE_NAME_COLOR = "profile_badge_name_color"
    const val BADGE_FONT = "profile_badge_font"
    const val BADGE_ICON = "profile_badge_icon"
    const val BADGE_ICON_COLOR = "profile_badge_icon_color"
    const val BADGE_AVATAR = "profile_badge_avatar"
}

/** How two colours are spread across a surface. */
enum class GradientType {
    VERTICAL,
    DIAGONAL,
    HORIZONTAL,
    RADIAL,
    SWEEP,
    ;

    fun brush(
        first: Color,
        second: Color,
    ): Brush =
        when (this) {
            VERTICAL -> Brush.verticalGradient(listOf(first, second))
            // Default start/end run from the top-left corner to the bottom-right one.
            DIAGONAL -> Brush.linearGradient(listOf(first, second))
            HORIZONTAL -> Brush.horizontalGradient(listOf(first, second))
            // The first colour is the circle in the middle.
            RADIAL -> Brush.radialGradient(listOf(first, second))
            // Closed loop, so there is no seam where the sweep meets its start.
            SWEEP -> Brush.sweepGradient(listOf(first, second, first))
        }
}

/**
 * A solid colour, or a two-colour gradient. Stored as one string:
 * `SOLID;RRGGBB` or `GRADIENT;TYPE;RRGGBB;RRGGBB`.
 */
@Immutable
data class ColorFill(
    val gradient: Boolean,
    val type: GradientType,
    val first: Color,
    val second: Color,
) {
    val brush: Brush get() = if (gradient) type.brush(first, second) else Brush.verticalGradient(listOf(first, first))

    /** One colour standing for the whole fill: what surfaces, text contrast and seeds derive from. */
    val base: Color get() = if (gradient) lerp(first, second, 0.5f) else first

    fun encode(): String =
        if (gradient) {
            "GRADIENT;${type.name};${first.toHex()};${second.toHex()}"
        } else {
            "SOLID;${first.toHex()}"
        }

    companion object {
        fun decode(raw: String?): ColorFill? {
            if (raw.isNullOrBlank()) return null
            val parts = raw.split(';')
            return when (parts.firstOrNull()) {
                "SOLID" -> {
                    val c = parts.getOrNull(1)?.let { hexToColor(it) } ?: return null
                    ColorFill(false, GradientType.VERTICAL, c, c)
                }
                "GRADIENT" -> {
                    val type = GradientType.entries.firstOrNull { it.name == parts.getOrNull(1) } ?: GradientType.VERTICAL
                    val a = parts.getOrNull(2)?.let { hexToColor(it) } ?: return null
                    val b = parts.getOrNull(3)?.let { hexToColor(it) } ?: return null
                    ColorFill(true, type, a, b)
                }
                else -> null
            }
        }
    }
}

/** Six hex digits, no alpha, upper case. */
fun Color.toHex(): String = (toArgb() and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()

/** Parses `RRGGBB` / `#RRGGBB` / `AARRGGBB`; null when it is not a colour. */
fun hexToColor(hex: String): Color? {
    val clean = hex.trim().removePrefix("#")
    val argb =
        when (clean.length) {
            6 -> "FF$clean"
            8 -> clean
            else -> return null
        }
    return argb.toLongOrNull(16)?.let { Color(it) }
}

/**
 * The page's own brush when the user picked a custom fill; null for the preset themes. Anything
 * that paints the page background behind content should paint this instead when it is set.
 */
val LocalPageBrush = staticCompositionLocalOf<Brush?> { null }

/**
 * The accent as a gradient, when the user picked one for Theme color; null otherwise. Used by the
 * few accent surfaces that can carry a gradient (selected tab, page titles). Everything else keeps
 * the solid primary, which is seeded from the gradient's first colour.
 */
val LocalAccentBrush = staticCompositionLocalOf<Brush?> { null }
