package com.maxrave.simpmusic.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

/**
 * Clips artwork to [shape] and lays over it the edge Apple Music gives every cover.
 *
 * Measured off Apple Music screenshots: a grey ring about one point wide, clearly visible on a
 * black cover (roughly #444 on black) and still there as a grey line on a pale one. It is drawn
 * over the artwork itself, inside the clip, in a mid grey — so it lightens a dark cover's edge and
 * darkens a pale one, and reads on either theme without knowing which is on.
 *
 * The ring sits wholly INSIDE the edge, on an outline inset by half its width, rather than centred
 * on the edge and half clipped away: centred, the clip's anti-aliasing let the outer half bleed
 * through at the rounded corners, so the corners came out heavier than the straight sides. Its
 * outline is built once per size, not per frame.
 */
fun Modifier.artworkFrame(shape: Shape): Modifier =
    clip(shape).drawWithCache {
        val width = 1.dp.toPx()
        val stroke = Stroke(width = width)
        val inset = width / 2f
        val outline =
            shape.createOutline(
                Size((size.width - width).coerceAtLeast(0f), (size.height - width).coerceAtLeast(0f)),
                layoutDirection,
                this,
            )
        onDrawWithContent {
            drawContent()
            translate(inset, inset) {
                drawOutline(outline, color = ArtworkEdge, style = stroke)
            }
        }
    }

private val ArtworkEdge = Color(0x5C808080)
