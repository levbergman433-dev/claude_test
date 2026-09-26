package com.maxrave.simpmusic.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.inter_bold
import simpmusic.composeapp.generated.resources.inter_medium
import simpmusic.composeapp.generated.resources.inter_regular
import simpmusic.composeapp.generated.resources.inter_semibold
import simpmusic.composeapp.generated.resources.poppins_medium

/**
 * True (default) sets the app in Inter, the closest freely licensed match to Apple's San Francisco
 * (SF itself may only be used on Apple platforms). False keeps SimpMusic's original Poppins.
 */
val LocalUseInter = staticCompositionLocalOf { true }

/**
 * Inter ships four real weights, so Normal text is actually regular and bold is not synthesised.
 * Poppins keeps its original single file, mapped to Normal as upstream always did.
 */
@Composable
fun fontFamily(): FontFamily =
    if (LocalUseInter.current) {
        FontFamily(
            Font(Res.font.inter_regular, FontWeight.Normal, FontStyle.Normal),
            Font(Res.font.inter_medium, FontWeight.Medium, FontStyle.Normal),
            Font(Res.font.inter_semibold, FontWeight.SemiBold, FontStyle.Normal),
            Font(Res.font.inter_bold, FontWeight.Bold, FontStyle.Normal),
        )
    } else {
        FontFamily(
            Font(Res.font.poppins_medium, FontWeight.Normal, FontStyle.Normal),
        )
    }

/**
 * When true, [typo] keeps the original always-light text colors (pure white titles, #A8A8A8 body)
 * regardless of theme. Immersive screens drawn over dark artwork provide `true` so their text stays
 * readable at light theme. Everything else leaves it false and gets theme-aware colors.
 */
val LocalForceDarkText = staticCompositionLocalOf { false }

@Composable
fun typo(
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    forceDark: Boolean = LocalForceDarkText.current,
): Typography {
    val fontFamily = fontFamily()

    // Titles were pure white, everything else muted gray (#A8A8A8) in the old dark-only palette.
    // forceDark keeps that; otherwise both come from the scheme (theme-aware light/dark).
    val titleColor = if (forceDark) Color.White else colorScheme.onBackground
    val bodyColor = if (forceDark) Color(0xFFA8A8A8) else colorScheme.onSurfaceVariant

    val typo =
        Typography(
            /***
             * This typo().is use for the title of the Playlist, Artist, Song, Album, etc. in Home, Mood, Genre, Playlist, etc.
             */
            titleSmall =
                TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = fontFamily,
                    color = titleColor,
                ),
            titleMedium =
                TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = fontFamily,
                    color = titleColor,
                ),
            titleLarge =
                TextStyle(
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    color = titleColor,
                ),
            bodySmall =
                TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = fontFamily,
                    color = bodyColor,
                ),
            bodyMedium =
                TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = fontFamily,
                    color = bodyColor,
                ),
            bodyLarge =
                TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = fontFamily,
                    color = bodyColor,
                ),
            displayLarge =
                TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = fontFamily,
                    color = bodyColor,
                ),
            headlineMedium =
                TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    color = bodyColor,
                ),
            headlineLarge =
                TextStyle(
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    color = bodyColor,
                ),
            labelMedium =
                TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    color = bodyColor,
                ),
            labelSmall =
                TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = fontFamily,
                    color = bodyColor,
                ),
            // ...
        )
    return typo
}
