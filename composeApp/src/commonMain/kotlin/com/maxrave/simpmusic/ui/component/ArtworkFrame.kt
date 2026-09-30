package com.maxrave.simpmusic.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Clips artwork to [shape] and lays the hairline edge Apple Music gives every cover over it.
 *
 * Without it a dark cover on a dark page (or a pale one on a light page) has no edge at all and
 * the tile melts into the background. The line is drawn over the artwork itself, inside the clip,
 * in a mid grey at low alpha — so it lightens a dark cover's edge and darkens a pale one, and reads
 * on either theme without knowing which is on. Its outline is built once per size, not per frame.
 */
fun Modifier.artworkFrame(shape: Shape): Modifier =
    clip(shape).drawWithCache {
        // Centred on the edge, so half of it is clipped away: the visible line is half this.
        val stroke = Stroke(width = 1.5.dp.toPx())
        val outline = shape.createOutline(size, layoutDirection, this)
        onDrawWithContent {
            drawContent()
            drawOutline(outline, color = ArtworkEdge, style = stroke)
        }
    }

private val ArtworkEdge = Color(0x38808080)
