package com.maxrave.simpmusic.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A picture chosen as the page background.
 *
 * It rides the same [LocalPageBrush] a gradient page does, so everything that already paints "the
 * page" — the scaffold, the top-bar slices, the glass backdrop — shows the picture with no changes
 * of its own. Text contrast and surface tint come from the picture's average colour after dimming.
 */
@Immutable
class PageImage(
    val brush: Brush,
    /** The picture's average colour, veil included: what surfaces derive from. */
    val base: Color,
    /** True when text on it is light (a dark theme). */
    val isDark: Boolean,
) {
    companion object {
        /** Longest side kept in memory: enough for a phone or a laptop window, not a 48 MP photo. */
        private const val MAX_SIDE = 1600

        /** Shrinks a picked photo to [MAX_SIDE] once, so the full-size original is never held. */
        fun downscale(image: ImageBitmap): ImageBitmap {
            val longest = max(image.width, image.height)
            if (longest <= MAX_SIDE) return image
            val k = MAX_SIDE.toFloat() / longest
            return renderCover(image, IntSize((image.width * k).roundToInt(), (image.height * k).roundToInt()), Color.Transparent)
        }

        /**
         * Builds the page image from a decoded picture with a veil of [dim] (0..1) baked in.
         *
         * [lightText] forces white (true) or dark (false) text; null picks by the picture's own
         * brightness. The veil follows the text: black under white text, white under dark text, so
         * dimming always pushes the picture away from the text colour instead of towards it.
         */
        fun from(
            image: ImageBitmap,
            dim: Float,
            lightText: Boolean?,
        ): PageImage {
            val amount = dim.coerceIn(0f, 0.9f)
            // Light text unless the picture is clearly bright; photos vary too much to cut at WCAG's 18%.
            val dark = lightText ?: (averageColour(image, Color.Transparent).luminance() < 0.45f)
            val veil = (if (dark) Color.Black else Color.White).copy(alpha = amount)
            return PageImage(brush = CoverImageBrush(image, veil), base = averageColour(image, veil), isDark = dark)
        }

        private fun averageColour(
            image: ImageBitmap,
            veil: Color,
        ): Color {
            val side = 16
            val small = renderCover(image, IntSize(side, side), veil)
            val pixels = IntArray(side * side)
            small.readPixels(pixels)
            var r = 0L
            var g = 0L
            var b = 0L
            for (p in pixels) {
                r += (p shr 16) and 0xFF
                g += (p shr 8) and 0xFF
                b += p and 0xFF
            }
            val n = pixels.size
            return Color((r / n).toInt(), (g / n).toInt(), (b / n).toInt())
        }
    }
}

/**
 * Draws [image] centre-cropped to fill whatever it paints, like `ContentScale.Crop`.
 *
 * A plain [ImageShader] cannot scale in common code, so the cropped picture is rendered once at
 * the size asked for and cached: every consumer paints at the window size, so that is one render
 * per window size rather than one per frame.
 */
private class CoverImageBrush(
    private val image: ImageBitmap,
    private val veil: Color,
) : ShaderBrush() {
    private var cachedSize: IntSize? = null
    private var cachedShader: Shader? = null

    override fun createShader(size: Size): Shader {
        val target = IntSize(max(1, size.width.roundToInt()), max(1, size.height.roundToInt()))
        cachedShader?.takeIf { cachedSize == target }?.let { return it }
        val shader = ImageShader(renderCover(image, target, veil))
        cachedSize = target
        cachedShader = shader
        return shader
    }

    override fun equals(other: Any?): Boolean = other is CoverImageBrush && other.image == image && other.veil == veil

    override fun hashCode(): Int = image.hashCode() * 31 + veil.hashCode()
}

private fun renderCover(
    image: ImageBitmap,
    target: IntSize,
    veil: Color,
): ImageBitmap {
    val out = ImageBitmap(target.width, target.height)
    val canvas = Canvas(out)
    val scale = max(target.width.toFloat() / image.width, target.height.toFloat() / image.height)
    val srcW = (target.width / scale).roundToInt().coerceIn(1, image.width)
    val srcH = (target.height / scale).roundToInt().coerceIn(1, image.height)
    val srcOffset = IntOffset((image.width - srcW) / 2, (image.height - srcH) / 2)
    canvas.drawImageRect(
        image = image,
        srcOffset = srcOffset,
        srcSize = IntSize(srcW, srcH),
        dstOffset = IntOffset.Zero,
        dstSize = target,
        paint = Paint().apply { filterQuality = FilterQuality.Medium },
    )
    if (veil.alpha > 0f) {
        canvas.drawRect(
            0f,
            0f,
            target.width.toFloat(),
            target.height.toFloat(),
            Paint().apply { color = veil },
        )
    }
    return out
}
