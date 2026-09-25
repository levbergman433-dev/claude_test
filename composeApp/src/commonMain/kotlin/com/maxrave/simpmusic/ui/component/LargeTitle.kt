package com.maxrave.simpmusic.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.poppins_bold

/**
 * Apple Music-style large page title ("Home", "Library"): big, heavy and slightly tightened, set
 * in the real Poppins Bold cut rather than a synthesised bold of the medium weight the rest of the
 * type scale uses. Shown when the Large titles setting is on ([com.maxrave.simpmusic.ui.theme.LocalLargeTitles]).
 */
@Composable
fun largeTitleStyle(): TextStyle =
    TextStyle(
        fontFamily = FontFamily(Font(Res.font.poppins_bold, FontWeight.Bold, FontStyle.Normal)),
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.4).sp,
        color = MaterialTheme.colorScheme.onBackground,
    )
