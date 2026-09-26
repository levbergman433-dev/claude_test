package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.theme.ColorFill
import com.maxrave.simpmusic.ui.theme.GradientType
import com.maxrave.simpmusic.ui.theme.hexToColor
import com.maxrave.simpmusic.ui.theme.toHex
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.cancel
import simpmusic.composeapp.generated.resources.fill_color_1
import simpmusic.composeapp.generated.resources.fill_color_2
import simpmusic.composeapp.generated.resources.fill_gradient
import simpmusic.composeapp.generated.resources.fill_solid
import simpmusic.composeapp.generated.resources.gradient_diagonal
import simpmusic.composeapp.generated.resources.gradient_drag_hint
import simpmusic.composeapp.generated.resources.gradient_horizontal
import simpmusic.composeapp.generated.resources.gradient_radial
import simpmusic.composeapp.generated.resources.gradient_sweep
import simpmusic.composeapp.generated.resources.gradient_vertical
import simpmusic.composeapp.generated.resources.save
import kotlin.math.max
import kotlin.math.min

private val presetSwatches =
    listOf(
        "000000", "1C1C1E", "3A3A3C", "8E8E93", "FFFFFF",
        "FA2D48", "EF6C9B", "9B72CF", "5E5CE6", "4C82EF",
        "8ECAE6", "26A69A", "66BB6A", "FFCA28", "F4A340",
        "0B1224", "170F1F", "0D1712", "1B1511", "F4ECDD",
    )

private fun GradientType.label(): StringResource =
    when (this) {
        GradientType.VERTICAL -> Res.string.gradient_vertical
        GradientType.DIAGONAL -> Res.string.gradient_diagonal
        GradientType.HORIZONTAL -> Res.string.gradient_horizontal
        GradientType.RADIAL -> Res.string.gradient_radial
        GradientType.SWEEP -> Res.string.gradient_sweep
    }

/**
 * Editor for a [ColorFill]: Solid or Gradient (when [allowGradient]), the gradient's shape, and a
 * colour picker (saturation/brightness square, hue slider, swatches, hex) for each colour stop.
 */
@Composable
fun ColorFillDialog(
    title: String,
    initial: ColorFill,
    allowGradient: Boolean,
    onDismiss: () -> Unit,
    onSave: (ColorFill) -> Unit,
) {
    var gradient by remember { mutableStateOf(allowGradient && initial.gradient) }
    var type by remember { mutableStateOf(initial.type) }
    val stops = remember { mutableStateListOf(initial.first, initial.second) }
    var editing by remember { mutableIntStateOf(0) }
    val current = stops[if (gradient) editing else 0]

    var focusX by remember { mutableFloatStateOf(initial.focusX) }
    var focusY by remember { mutableFloatStateOf(initial.focusY) }
    val fill = ColorFill(gradient, type, stops[0], if (gradient) stops[1] else stops[0], focusX, focusY)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = typo().titleSmall) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (allowGradient) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !gradient,
                            onClick = { gradient = false },
                            label = { Text(stringResource(Res.string.fill_solid)) },
                        )
                        FilterChip(
                            selected = gradient,
                            onClick = { gradient = true },
                            label = { Text(stringResource(Res.string.fill_gradient)) },
                        )
                    }
                }
                // Live preview of the whole fill. With a gradient, dragging on it moves the
                // gradient's point (the handle): the centre of a circle or sweep, or where the
                // blend sits for the straight ones.
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(if (gradient) 150.dp else 84.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(fill.brush)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                            .then(
                                if (gradient) {
                                    Modifier
                                        .pointerInput(Unit) {
                                            fun update(p: Offset) {
                                                focusX = (p.x / size.width).coerceIn(0f, 1f)
                                                focusY = (p.y / size.height).coerceIn(0f, 1f)
                                            }
                                            awaitEachGesture {
                                                val down = awaitFirstDown()
                                                update(down.position)
                                                drag(down.id) { change ->
                                                    update(change.position)
                                                    change.consume()
                                                }
                                            }
                                        }.drawWithContent {
                                            drawContent()
                                            val c = Offset(focusX * size.width, focusY * size.height)
                                            drawCircle(Color.Black.copy(alpha = 0.35f), radius = 13.dp.toPx(), center = c)
                                            drawCircle(Color.White, radius = 11.dp.toPx(), center = c, style = Stroke(3.dp.toPx()))
                                        }
                                } else {
                                    Modifier
                                },
                            ),
                )
                if (gradient) {
                    Text(
                        text = stringResource(Res.string.gradient_drag_hint),
                        style = typo().bodySmall,
                    )
                }
                if (gradient) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        GradientType.entries.forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(stringResource(t.label())) },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(Res.string.fill_color_1, Res.string.fill_color_2).forEachIndexed { index, label ->
                            FilterChip(
                                selected = editing == index,
                                onClick = { editing = index },
                                leadingIcon = {
                                    Box(
                                        Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(stops[index])
                                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                    )
                                },
                                label = { Text(stringResource(label)) },
                            )
                        }
                    }
                }
                // key() so switching stops re-seeds the picker's hue/saturation/brightness.
                androidx.compose.runtime.key(if (gradient) editing else 0) {
                    ColorPicker(
                        color = current,
                        onColorChange = { stops[if (gradient) editing else 0] = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(fill) }) { Text(stringResource(Res.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        },
    )
}

/** Saturation/brightness square + hue slider + swatches + hex field for one colour. */
@Composable
fun ColorPicker(
    color: Color,
    onColorChange: (Color) -> Unit,
) {
    val initialHsv = remember { color.toHsv() }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var sat by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }
    var hexText by remember { mutableStateOf(color.toHex()) }

    fun emit() {
        val c = Color.hsv(hue, sat, value)
        hexText = c.toHex()
        onColorChange(c)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Saturation left to right, brightness top to bottom, at the current hue.
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f))))
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                    .pointerInput(Unit) {
                        fun update(p: Offset) {
                            sat = (p.x / size.width).coerceIn(0f, 1f)
                            value = 1f - (p.y / size.height).coerceIn(0f, 1f)
                            emit()
                        }
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            update(down.position)
                            drag(down.id) { change ->
                                update(change.position)
                                change.consume()
                            }
                        }
                    }.drawWithContent {
                        drawContent()
                        val center = Offset(sat * size.width, (1f - value) * size.height)
                        drawCircle(Color.White, radius = 11.dp.toPx(), center = center, style = Stroke(3.dp.toPx()))
                        drawCircle(Color.Black.copy(alpha = 0.35f), radius = 12.5.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
                    },
        )
        // Hue slider.
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map { Color.hsv(it % 360f, 1f, 1f) },
                        ),
                    ).pointerInput(Unit) {
                        fun update(p: Offset) {
                            hue = ((p.x / size.width).coerceIn(0f, 1f) * 360f).coerceAtMost(359.9f)
                            emit()
                        }
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            update(down.position)
                            drag(down.id) { change ->
                                update(change.position)
                                change.consume()
                            }
                        }
                    }.drawWithContent {
                        drawContent()
                        val x = (hue / 360f) * size.width
                        drawCircle(Color.White, radius = size.height / 2 - 2.dp.toPx(), center = Offset(x, size.height / 2), style = Stroke(3.dp.toPx()))
                    },
        )
        // Swatches.
        presetSwatches.chunked(5).forEach { rowHex ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowHex.forEach { hex ->
                    val swatch = hexToColor(hex) ?: Color.Gray
                    Box(
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(swatch)
                                .border(
                                    width = if (hexText.equals(hex, ignoreCase = true)) 3.dp else 1.dp,
                                    color =
                                        if (hexText.equals(hex, ignoreCase = true)) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                    shape = CircleShape,
                                ).clickable {
                                    val hsv = swatch.toHsv()
                                    // Keep the hue of greys, which have none of their own.
                                    if (hsv[1] > 0f) hue = hsv[0]
                                    sat = hsv[1]
                                    value = hsv[2]
                                    emit()
                                },
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.hsv(hue, sat, value))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
            TextField(
                value = hexText,
                onValueChange = { text ->
                    hexText = text.removePrefix("#").take(6).uppercase()
                    hexToColor(hexText)?.let { parsed ->
                        val hsv = parsed.toHsv()
                        if (hsv[1] > 0f) hue = hsv[0]
                        sat = hsv[1]
                        value = hsv[2]
                        onColorChange(parsed)
                    }
                },
                prefix = { Text("#") },
                singleLine = true,
                isError = hexToColor(hexText) == null,
                textStyle = typo().bodyLarge.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(2.dp))
    }
}

/** [hue 0..360, saturation 0..1, value 0..1]. */
private fun Color.toHsv(): FloatArray {
    val r = red
    val g = green
    val b = blue
    val maxC = max(r, max(g, b))
    val minC = min(r, min(g, b))
    val delta = maxC - minC
    val h =
        when {
            delta == 0f -> 0f
            maxC == r -> 60f * (((g - b) / delta) % 6f)
            maxC == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }.let { if (it < 0f) it + 360f else it }
    val s = if (maxC == 0f) 0f else delta / maxC
    // Color.hsv rejects 360, which the maths above can land on.
    return floatArrayOf(h.coerceIn(0f, 359.99f), s, maxC)
}

/** Text colour that stays readable on [background]. */
fun readableOn(background: Color): Color = if (background.luminance() > 0.5f) Color.Black else Color.White
