package com.maxrave.simpmusic.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
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

    // Size of the ⋮ menu button in the Home top bar: "M", "L" (default) or "XL".
    const val MENU_BUTTON_SIZE = "menu_button_size"

    // Look of pop-up menus: MENU_STYLE_LIQUID (default) or MENU_STYLE_FROSTED.
    const val MENU_STYLE = "menu_style"

    // Look of the top bar once a page scrolls (see topBarSurface).
    const val TOP_BAR_STYLE = "top_bar_style"

    // A picture as the page background: a file in app storage, and how much it is dimmed (0-80).
    const val PAGE_IMAGE = "page_image"
    const val PAGE_IMAGE_DIM = "page_image_dim"

    /** Text on the picture: "AUTO" (by its brightness), "LIGHT" (white) or "DARK". */
    const val PAGE_IMAGE_TEXT = "page_image_text"

    // Night mode (AOD) player. Clock and lock screen and burn-in protection default to on (a
    // missing value counts as "TRUE"); auto-dim defaults to off ("0" seconds).
    const val AOD_CLOCK = "aod_clock"
    const val AOD_CLOCK_STYLE = "aod_clock_style"
    const val AOD_LOCK_SCREEN = "aod_lock_screen"
    const val AOD_BURN_IN = "aod_burn_in"
    const val AOD_AUTO_DIM = "aod_auto_dim"

    const val BADGE_ENABLED = "profile_badge_enabled"
    const val BADGE_NAME = "profile_badge_name"
    const val BADGE_NAME_COLOR = "profile_badge_name_color"
    const val BADGE_FONT = "profile_badge_font"
    const val BADGE_ICON = "profile_badge_icon"
    const val BADGE_ICON_COLOR = "profile_badge_icon_color"
    const val BADGE_AVATAR = "profile_badge_avatar"
}

/** Menus drawn as the same liquid glass as the nav bar (needs the liquid-glass setting on). */
const val MENU_STYLE_LIQUID = "LIQUID"

/** Menus as a frosted pop-up: a blurred window on Android 12+, a solid pop-up elsewhere. */
const val MENU_STYLE_FROSTED = "FROSTED"

/** How two colours are spread across a surface. */
enum class GradientType {
    VERTICAL,
    DIAGONAL,
    HORIZONTAL,
    RADIAL,
    SWEEP,
    ;

    /**
     * [focusX]/[focusY] (0..1) move the gradient: the centre of a circle or sweep, or, for the
     * straight ones, where the blend between the two colours sits along the gradient's direction.
     * The brush is resolved against whatever size it is drawn at.
     */
    fun brush(
        first: Color,
        second: Color,
        focusX: Float = 0.5f,
        focusY: Float = 0.5f,
    ): Brush {
        val fx = focusX.coerceIn(0f, 1f)
        val fy = focusY.coerceIn(0f, 1f)
        val mid = lerp(first, second, 0.5f)
        // Straight gradients: the halfway colour is placed at the focus, stretching one side.
        fun stops(t: Float) = arrayOf(0f to first, t.coerceIn(0.02f, 0.98f) to mid, 1f to second)
        return when (this) {
            VERTICAL -> Brush.verticalGradient(*stops(fy))
            DIAGONAL -> Brush.linearGradient(*stops((fx + fy) / 2f))
            HORIZONTAL -> Brush.horizontalGradient(*stops(fx))
            RADIAL ->
                object : ShaderBrush() {
                    override fun createShader(size: Size): Shader {
                        val center = Offset(fx * size.width, fy * size.height)
                        // Reach the farthest corner, so the outer colour fills the whole surface.
                        val radius =
                            listOf(
                                Offset(0f, 0f),
                                Offset(size.width, 0f),
                                Offset(0f, size.height),
                                Offset(size.width, size.height),
                            ).maxOf { (it - center).getDistance() }.coerceAtLeast(1f)
                        return RadialGradientShader(center, radius, listOf(first, second))
                    }
                }
            // Closed loop, so there is no seam where the sweep meets its start.
            SWEEP ->
                object : ShaderBrush() {
                    override fun createShader(size: Size): Shader =
                        SweepGradientShader(Offset(fx * size.width, fy * size.height), listOf(first, second, first))
                }
        }
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
    val focusX: Float = 0.5f,
    val focusY: Float = 0.5f,
) {
    val brush: Brush get() = if (gradient) type.brush(first, second, focusX, focusY) else Brush.verticalGradient(listOf(first, first))

    /** One colour standing for the whole fill: what surfaces, text contrast and seeds derive from. */
    val base: Color get() = if (gradient) lerp(first, second, 0.5f) else first

    /**
     * Whether text on this fill reads better dark than light.
     *
     * Decided by contrast against BOTH colours, keeping whichever text colour stays more legible at
     * the worse end of the gradient. The old rule judged only the midpoint and called anything under
     * 50% luminance "dark", which put white text on light gradients and on mid-tone colours (WCAG's
     * black/white crossover is around 18% luminance, not 50%).
     */
    fun prefersDarkText(): Boolean {
        fun contrast(
            a: Float,
            b: Float,
        ) = (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
        val l1 = first.luminance()
        val l2 = second.luminance()
        val withBlack = minOf(contrast(l1, 0f), contrast(l2, 0f))
        val withWhite = minOf(contrast(l1, 1f), contrast(l2, 1f))
        return withBlack > withWhite
    }

    fun encode(): String =
        if (gradient) {
            "GRADIENT;${type.name};${first.toHex()};${second.toHex()};$focusX;$focusY"
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
                    val fx = parts.getOrNull(4)?.toFloatOrNull() ?: 0.5f
                    val fy = parts.getOrNull(5)?.toFloatOrNull() ?: 0.5f
                    ColorFill(true, type, a, b, fx, fy)
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

const val AOD_CLOCK_GLASS = "GLASS"
const val AOD_CLOCK_THIN = "THIN"
const val AOD_CLOCK_BOLD = "BOLD"
const val AOD_CLOCK_MINIMAL = "MINIMAL"
