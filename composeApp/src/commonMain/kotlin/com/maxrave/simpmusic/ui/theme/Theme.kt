package com.maxrave.simpmusic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.simpmusic.expect.ui.SystemBarAppearanceEffect
import com.maxrave.simpmusic.expect.ui.platformDynamicColorScheme

/**
 * Semantic colors that sit outside the Material 3 ColorScheme.
 * Read them via [LocalAppColors] inside composables.
 */
@Immutable
data class AppColors(
    val favorite: Color,
    val lyricActive: Color,
    val shimmerBackground: Color,
    val shimmerLine: Color,
    val overlay: Color,
    val overlayHeavy: Color,
)

private val DarkAppColors =
    AppColors(
        favorite = favoriteColor,
        lyricActive = lyricActiveColor,
        shimmerBackground = shimmerBackground,
        shimmerLine = shimmerLine,
        overlay = overlay,
        overlayHeavy = blackMoreOverlay,
    )

// Overlays stay dark in both themes: they cover artwork, where content is always light.
private val LightAppColors =
    DarkAppColors.copy(
        shimmerBackground = shimmerBackgroundLight,
        shimmerLine = shimmerLineLight,
    )

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

/** True in dark theme. Provided by [AppTheme] so platform glass (blur/tint) can adapt reliably. */
val LocalIsDarkTheme = staticCompositionLocalOf { true }

/**
 * Whether liquid-glass surfaces may actually draw glass.
 *
 * Provided by [AppTheme] from the user's setting; the default is true so previews and anything
 * composed outside [AppTheme] keep today's look. The setting row is Android-only, so Desktop always
 * provides true — its capsule player and detail-screen buttons are glass by design, with no switch.
 */
val LocalLiquidGlassEnabled = staticCompositionLocalOf { true }

/**
 * Whether glass surfaces draw the Apple (iOS 26 Liquid Glass) look — clearer tint, a thin specular
 * rim and a touch of edge dispersion — rather than the original darker "Classic" frosted glass.
 * Provided by [AppTheme] from the Glass style setting.
 */
val LocalAppleGlass = staticCompositionLocalOf { true }

/**
 * True while the Battery saver setting is on: decorative, continuously-running work (animated
 * backgrounds, how often glass re-samples the content behind it) is reduced or frozen.
 */
val LocalBatterySaver = staticCompositionLocalOf { false }

/** True when top-level pages (Home, Library) use Apple Music-style large titles. */
val LocalLargeTitles = staticCompositionLocalOf { true }

/** True when Home and Library use the Apple Music page structure (see AppleMusicLayout.kt). */
val LocalAppleLayout = staticCompositionLocalOf { true }

/**
 * The dark scheme to use for immersive screens while the app itself is on the light theme.
 * Provided by [AppTheme], consumed by [ForceDarkContent]; null only outside of [AppTheme].
 */
val LocalForcedDarkColorScheme = staticCompositionLocalOf<ColorScheme?> { null }

/** Parses "RRGGBB" or "AARRGGBB" (optionally "#"-prefixed) into a [Color]; null if malformed. */
fun parseThemeColorHex(hex: String): Color? {
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
 * Neutral surfaces for the light theme, on a pure neutral-grey ramp (R=G=B, no seed tint);
 * primary/secondary/tertiary stay seed-derived. The page background is #FAFAFA (neutral tone 98,
 * the Material 3 stance) rather than pure white: a full-bleed #FFFFFF expanse glares on a large
 * desktop window, and even Apple — who pins systemBackground to white — puts #F2F2F7 behind
 * list pages. Pure white is kept for surfaceBright/surfaceContainerLowest so cards and sheets
 * still have a brighter-than-page tier to lift onto. Dark stays pure-black AMOLED: black does
 * not glare and saves OLED.
 */
private fun ColorScheme.withNeutralLightSurfaces(): ColorScheme =
    copy(
        background = Color(0xFFFAFAFA),
        onBackground = Color(0xFF1B1B1B),
        surface = Color(0xFFFAFAFA),
        onSurface = Color(0xFF1B1B1B),
        surfaceVariant = Color(0xFFE2E2E2),
        onSurfaceVariant = Color(0xFF474747),
        surfaceTint = primary,
        surfaceBright = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFDADADA),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF7F7F7),
        surfaceContainer = Color(0xFFF1F1F1),
        surfaceContainerHigh = Color(0xFFECECEC),
        surfaceContainerHighest = Color(0xFFE6E6E6),
        outline = Color(0xFF777777),
        outlineVariant = Color(0xFFC7C7C7),
        inverseSurface = Color(0xFF303030),
        inverseOnSurface = Color(0xFFF1F1F1),
    )

/**
 * Pins the accent roles to Apple Music red. Only the accent moves: surfaces, text and the rest of
 * the tonal ramp stay seed-derived, so the page keeps Apple's neutral black/white look with red
 * reserved for what is interactive or selected (tab icons, sliders, primary buttons).
 */
private fun ColorScheme.withAppleMusicAccent(): ColorScheme =
    copy(
        primary = appleMusicRed,
        onPrimary = Color.White,
        inversePrimary = appleMusicRed,
        surfaceTint = appleMusicRed,
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppTheme(
    themeMode: String = DataStoreManager.THEME_MODE_DARK,
    themeColorSource: String = DataStoreManager.THEME_COLOR_DEFAULT,
    customThemeColor: Color? = null,
    liquidGlassEnabled: Boolean = true,
    glassStyle: String = DataStoreManager.GLASS_STYLE_APPLE,
    batterySaver: Boolean = false,
    largeTitles: Boolean = true,
    appleLayout: Boolean = true,
    content:
        @Composable()
        () -> Unit,
) {
    val isDark = isDarkTheme(themeMode)
    val surfaceTheme = surfaceThemeFor(themeMode)
    val wallpaperScheme =
        if (themeColorSource == DataStoreManager.THEME_COLOR_WALLPAPER) {
            platformDynamicColorScheme(isDark)
        } else {
            null
        }
    val isAppleMusicColor = themeColorSource == DataStoreManager.THEME_COLOR_APPLE_MUSIC
    val seedColor =
        when (themeColorSource) {
            DataStoreManager.THEME_COLOR_CUSTOM -> customThemeColor ?: seed
            DataStoreManager.THEME_COLOR_APPLE_MUSIC -> appleMusicRed
            else -> seed
        }
    // Symmetric base: dark pins background/surface to pure black via isAmoled; light pins them to
    // pure white with a neutral-grey ramp (the seed otherwise tints the light neutrals warm/cream).
    val seededScheme =
        wallpaperScheme
            ?: rememberDynamicColorScheme(
                seedColor = seedColor,
                isDark = isDark,
                isAmoled = isDark,
                style = PaletteStyle.TonalSpot,
                modifyColorScheme = { cs ->
                    val base = if (isDark) cs else cs.withNeutralLightSurfaces()
                    if (isAppleMusicColor) base.withAppleMusicAccent() else base
                },
            )
    // Applied after, not inside modifyColorScheme: materialkolor only rebuilds when the seed or
    // dark/light changes, so switching Dark -> Midnight (both dark) would otherwise do nothing.
    val colorScheme =
        remember(seededScheme, surfaceTheme, isDark) {
            if (surfaceTheme != null) seededScheme.withSurfaceTheme(surfaceTheme, isDark) else seededScheme
        }
    // Immersive screens stay dark even at light theme (see [ForceDarkContent]). Resolve their scheme
    // once here instead of letting every such subtree build a palette of its own.
    val forcedDarkScheme =
        if (isDark) {
            colorScheme
        } else {
            rememberDynamicColorScheme(
                seedColor = seedColor,
                isDark = true,
                isAmoled = true,
                style = PaletteStyle.TonalSpot,
                modifyColorScheme = { cs -> if (isAppleMusicColor) cs.withAppleMusicAccent() else cs },
            )
        }
    SystemBarAppearanceEffect(isDark)
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        content = {
            CompositionLocalProvider(
                LocalRippleConfiguration provides SoftRippleConfiguration,
                LocalContentColor provides colorScheme.onSurfaceVariant,
                LocalAppColors provides if (isDark) DarkAppColors else LightAppColors,
                LocalIsDarkTheme provides isDark,
                LocalForcedDarkColorScheme provides forcedDarkScheme,
                LocalLiquidGlassEnabled provides liquidGlassEnabled,
                LocalAppleGlass provides (glassStyle != DataStoreManager.GLASS_STYLE_CLASSIC),
                LocalBatterySaver provides batterySaver,
                LocalLargeTitles provides largeTitles,
                LocalAppleLayout provides appleLayout,
                content = content,
            )
        },
        typography = typo(colorScheme),
    )
}

/**
 * A quieter ripple than the Material default, applied app-wide.
 *
 * Material 3's own alphas are tuned for light surfaces; on the near-black background this app
 * uses they read as a grey flash rather than a touch response. Each value is roughly 40% of
 * the default — enough to register the touch, not enough to wash the surface.
 *
 * The colour is left unspecified on purpose, so the ripple keeps deriving from LocalContentColor
 * and stays correct inside [ForceDarkContent] subtrees, which run a different scheme.
 *
 * This reaches bare `Modifier.clickable` too, not just Material components: MaterialTheme
 * already provides `material3.ripple()` as LocalIndication, and that is the one indication that
 * reads this configuration. Nothing extra has to be provided for it.
 *
 * The (color, rippleAlpha) constructor is deprecated in favour of one that also takes a `focus`,
 * but that three-argument version is internal to material3, and the two public replacements —
 * (color) and (focus, color) — drop rippleAlpha entirely. Adjusting ripple alpha therefore has
 * no non-deprecated path in this version, so the warning is suppressed rather than designed
 * around. Revisit once material3 opens up the full constructor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
private val SoftRippleConfiguration =
    RippleConfiguration(
        // Colour is deliberately left alone. Ripple derives it from LocalContentColor, which is
        // light on the dark theme and dark on the light one — always contrasting with what it
        // covers. Pinning a dark grey here instead made the ripple paint *darker* than the black
        // surface underneath, so a tap read as a sooty smudge rather than a highlight.
        // Alpha is the only lever that softens without changing which way the contrast runs.
        rippleAlpha =
            RippleAlpha(
                draggedAlpha = 0.06f,
                focusedAlpha = 0.04f,
                hoveredAlpha = 0.03f,
                pressedAlpha = 0.04f,
            ),
    )

/**
 * Wraps immersive screens — artist, album, playlist, local playlist, podcast and the players — which
 * always draw over dark artwork and must therefore render dark even on the light theme.
 *
 * [LocalForceDarkText] alone is not enough: it only recolors text through [typo]. Icons, buttons and
 * every other Material default read [LocalContentColor] and [MaterialTheme.colorScheme], which at
 * light theme resolve to dark-on-light and turn grey and unreadable over the artwork. Providing the
 * dark scheme here fixes the whole subtree in one place.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ForceDarkContent(content: @Composable () -> Unit) {
    val darkScheme = LocalForcedDarkColorScheme.current ?: MaterialTheme.colorScheme
    MaterialExpressiveTheme(
        colorScheme = darkScheme,
        content = {
            CompositionLocalProvider(
                LocalForceDarkText provides true,
                LocalIsDarkTheme provides true,
                LocalContentColor provides darkScheme.onSurfaceVariant,
                LocalAppColors provides DarkAppColors,
                content = content,
            )
        },
        typography = typo(darkScheme, forceDark = true),
    )
}

/**
 * Resolves the stored theme mode to a plain boolean.
 *
 * Pulled out of [AppTheme] so chrome living *outside* it can ask the same question — the desktop
 * title bar is drawn before AppTheme is entered, so it has no MaterialTheme to read and would
 * otherwise need its own copy of this `when`.
 */
@Composable
fun isDarkTheme(themeMode: String): Boolean =
    when (themeMode) {
        DataStoreManager.THEME_MODE_LIGHT, DataStoreManager.THEME_MODE_SEPIA -> false
        DataStoreManager.THEME_MODE_SYSTEM -> isSystemInDarkTheme()
        else -> true
    }

/**
 * The page colour of a colour theme; null for plain Dark / Light / System, which keep their own
 * pure-black and neutral-grey surfaces.
 */
private fun surfaceThemeFor(themeMode: String): Color? =
    when (themeMode) {
        // Apple's dark mode: the elevated grey of iOS rather than OLED black.
        DataStoreManager.THEME_MODE_GRAPHITE -> Color(0xFF1C1C1E)
        DataStoreManager.THEME_MODE_MIDNIGHT -> Color(0xFF0B1224)
        DataStoreManager.THEME_MODE_FOREST -> Color(0xFF0D1712)
        DataStoreManager.THEME_MODE_PLUM -> Color(0xFF170F1F)
        DataStoreManager.THEME_MODE_MOCHA -> Color(0xFF1B1511)
        DataStoreManager.THEME_MODE_SEPIA -> Color(0xFFF4ECDD)
        else -> null
    }

/**
 * Rebuilds every surface role from one page colour. Containers step away from the page towards
 * white (dark themes) or black (light themes), so cards, sheets and bars keep the same relative
 * lift as on the default themes, but all carry the theme's hue. Accent roles stay seed-derived.
 */
private fun ColorScheme.withSurfaceTheme(
    page: Color,
    dark: Boolean,
): ColorScheme {
    val toward = if (dark) Color.White else Color.Black
    fun step(fraction: Float) = lerp(page, toward, fraction)
    val onPage = if (dark) Color(0xFFF2F2F2) else Color(0xFF1F1A14)
    val onVariant = if (dark) lerp(onPage, page, 0.30f) else lerp(onPage, page, 0.25f)
    return copy(
        background = page,
        onBackground = onPage,
        surface = page,
        onSurface = onPage,
        surfaceVariant = step(0.14f),
        onSurfaceVariant = onVariant,
        surfaceTint = primary,
        surfaceBright = step(0.16f),
        surfaceDim = if (dark) page else step(0.10f),
        surfaceContainerLowest = if (dark) lerp(page, Color.Black, 0.35f) else lerp(page, Color.White, 0.6f),
        surfaceContainerLow = step(0.03f),
        surfaceContainer = step(0.06f),
        surfaceContainerHigh = step(0.09f),
        surfaceContainerHighest = step(0.12f),
        outline = step(0.40f),
        outlineVariant = step(0.20f),
        inverseSurface = onPage,
        inverseOnSurface = page,
    )
}
