package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import kotlinx.coroutines.delay
import com.maxrave.simpmusic.expect.ui.PlatformBackHandler
import kotlin.math.roundToInt

/**
 * Lets a [GlassDropdownMenu] open as REAL liquid glass — the same lens, blur and rim as the nav bar
 * and mini player.
 *
 * That glass can only refract the page layer inside the main window, and a normal dropdown opens
 * in a pop-up window of its own where that layer does not exist. So while a host is provided, the
 * menu is not a pop-up: it is drawn by [GlassMenuOverlay], which sits at the root of the app next
 * to the nav bar, a sibling of the page's backdrop source (never inside it, which would feed the
 * glass back into itself).
 *
 * Provided only when the menu style setting is Liquid glass AND the page is actually being
 * recorded as a backdrop (the liquid-glass setting is on); otherwise menus stay pop-ups.
 */
@Stable
class GlassMenuHost(
    val backdrop: LayerBackdrop,
) {
    /** Window position of the backdrop source; set by [glassMenuHostSource]. */
    internal var sourceOrigin = Offset.Zero

    /**
     * What the open menu or sheet actually refracts: ONE snapshot of the page, taken when it opens.
     *
     * Refracting the live page makes the glass re-render — blur, lens and all, over the whole
     * sheet — on every frame anything underneath changes: a canvas video, a scrolling title, the
     * progress bar. Over the player that is every single frame, which is what made the sheet
     * there lag. The page under an open sheet is behind a scrim and cannot be touched, so a still
     * of it looks the same, and the glass now only redraws when the sheet itself moves.
     */
    internal val frozen = FrozenBackdrop()

    /** A menu or sheet is open (or opening). Hosts whose source is not always recorded use this to record it only then. */
    val isOpen: Boolean get() = active != null || activeSheet != null
    internal var active by mutableStateOf<ActiveGlassMenu?>(null)

    internal fun show(menu: ActiveGlassMenu) {
        active = menu
    }

    internal fun hide(token: Any) {
        if (active?.token === token) active = null
    }

    internal var activeSheet by mutableStateOf<ActiveGlassSheet?>(null)

    internal fun showSheet(sheet: ActiveGlassSheet) {
        activeSheet = sheet
    }

    internal fun hideSheet(token: Any) {
        if (activeSheet?.token === token) activeSheet = null
    }
}

/** Draws a still [image] of the backdrop source, placed where the source was in the window. */
@Stable
internal class FrozenBackdrop : Backdrop {
    var image by mutableStateOf<ImageBitmap?>(null)
    var origin = Offset.Zero

    override val isCoordinatesDependent: Boolean = true

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?,
    ) {
        val snapshot = image ?: return
        val position = coordinates?.positionInWindow() ?: return
        translate(origin.x - position.x, origin.y - position.y) {
            drawImage(snapshot)
        }
    }
}

/** Marks the backdrop source of [host]: records it for the glass and tracks where it sits. */
fun Modifier.glassMenuHostSource(host: GlassMenuHost): Modifier =
    this
        .onGloballyPositioned { host.sourceOrigin = it.positionInWindow() }
        .layerBackdrop(host.backdrop)

internal class ActiveGlassSheet(
    val token: Any,
    val onDismissRequest: () -> Unit,
    val content: @Composable ColumnScope.() -> Unit,
)

internal class ActiveGlassMenu(
    val token: Any,
    val anchor: Rect,
    val offset: DpOffset,
    val modifier: Modifier,
    val onDismissRequest: () -> Unit,
    val content: @Composable ColumnScope.() -> Unit,
)

val LocalGlassMenuHost = staticCompositionLocalOf<GlassMenuHost?> { null }

private val GlassMenuShape = RoundedCornerShape(24.dp)

/**
 * Draws the open glass sheet and menu, if any. Place it at the root of the window, after
 * everything else, outside the backdrop source.
 */
@Composable
fun GlassMenuOverlay(host: GlassMenuHost) {
    val open = host.active != null || host.activeSheet != null
    LaunchedEffect(open) {
        if (open) {
            // Snapshot on open (see [GlassMenuHost.frozen]). Until it lands the layers stay hidden,
            // which is a frame or two.
            // Two frames first: a source recorded only while open has to draw once before its
            // layer holds anything.
            repeat(2) { withFrameNanos { } }
            host.frozen.origin = host.sourceOrigin
            host.frozen.image = runCatching { host.backdrop.graphicsLayer.toImageBitmap() }.getOrNull()
        } else {
            // Kept through the sheet's slide-out, then released: it is a full-screen bitmap.
            delay(600)
            host.frozen.image = null
        }
    }
    if (host.frozen.image == null) return
    GlassSheetLayer(host)
    GlassMenuLayer(host)
}

/**
 * A bottom sheet as liquid glass, for the same reason menus are: a ModalBottomSheet is a window of
 * its own and cannot refract the page. Registers with the nearest [GlassMenuHost]; only call it
 * where [LocalGlassMenuHost] is non-null. [onDismissRequest] should simply stop showing the sheet —
 * the overlay plays the slide-out itself.
 */
@Composable
fun GlassSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val host = LocalGlassMenuHost.current ?: return
    val latestContent by rememberUpdatedState(content)
    val latestDismiss by rememberUpdatedState(onDismissRequest)
    val token = remember { Any() }
    DisposableEffect(host) {
        host.showSheet(
            ActiveGlassSheet(
                token = token,
                onDismissRequest = { latestDismiss() },
                content = { latestContent() },
            ),
        )
        onDispose { host.hideSheet(token) }
    }
}

private val GlassSheetShape = RoundedCornerShape(34.dp)

@Composable
private fun GlassSheetLayer(host: GlassMenuHost) {
    val target = host.activeSheet
    // The sheet on screen, which outlives its registration by the length of the slide-out.
    var shown by remember { mutableStateOf<ActiveGlassSheet?>(null) }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(target) {
        if (target != null) {
            shown = target
            progress.animateTo(1f, spring(dampingRatio = 0.86f, stiffness = 420f))
        } else if (shown != null) {
            progress.animateTo(0f, tween(220))
            shown = null
        }
    }
    val sheet = shown ?: return
    val scope = rememberCoroutineScope()
    var dragY by remember(sheet.token) { mutableFloatStateOf(0f) }
    var panelHeight by remember { mutableIntStateOf(0) }
    PlatformBackHandler(enabled = target != null) { sheet.onDismissRequest() }

    // Pull-down-to-close on top of the sheet's own scrolling: once the list is at its top, a
    // further pull moves the whole sheet; releasing far or fast enough closes it.
    val dragToDismiss =
        remember(sheet.token) {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (available.y < 0f && dragY > 0f) {
                        val used = maxOf(available.y, -dragY)
                        dragY += used
                        return Offset(0f, used)
                    }
                    return Offset.Zero
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (available.y > 0f && source == NestedScrollSource.UserInput) {
                        dragY += available.y
                        return Offset(0f, available.y)
                    }
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (dragY <= 0f) return Velocity.Zero
                    if (dragY > panelHeight * 0.25f || available.y > 1800f) {
                        sheet.onDismissRequest()
                    } else {
                        scope.launch { animate(dragY, 0f) { value, _ -> dragY = value } }
                    }
                    return available
                }
            }
        }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = progress.value }
                    .background(Color.Black.copy(alpha = 0.35f))
                    .pointerInput(sheet.token) { detectTapGestures { sheet.onDismissRequest() } },
        )
        val maxSheetHeight = maxHeight * 0.88f
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
                    .onSizeChanged { panelHeight = it.height }
                    .graphicsLayer {
                        translationY = (1f - progress.value) * (panelHeight + 64.dp.toPx()) + dragY
                    }.nestedScroll(dragToDismiss)
                    .liquidGlass(host.frozen, GlassSheetShape, interactive = false)
                    .clip(GlassSheetShape)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = sheet.content,
        )
    }
}

@Composable
private fun GlassMenuLayer(host: GlassMenuHost) {
    val menu = host.active ?: return
    var origin by remember { mutableStateOf(Offset.Zero) }
    PlatformBackHandler(enabled = true) { menu.onDismissRequest() }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { origin = it.positionInWindow() },
    ) {
        // Tapping anywhere outside the menu closes it, like the pop-up it replaces. A separate
        // sibling UNDER the menu, so a tap on the menu itself never reaches it.
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(menu) { detectTapGestures { menu.onDismissRequest() } },
        )
        val appear = remember(menu.token) { Animatable(0f) }
        LaunchedEffect(menu.token) { appear.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 700f)) }
        var opensUpward by remember(menu.token) { mutableStateOf(false) }
        var alignedToEnd by remember(menu.token) { mutableStateOf(false) }
        Layout(
            content = {
                Column(
                    modifier =
                        menu.modifier
                            .graphicsLayer {
                                val p = appear.value
                                alpha = p.coerceIn(0f, 1f)
                                scaleX = 0.9f + 0.1f * p
                                scaleY = 0.9f + 0.1f * p
                                // Grows out of the corner nearest the button that opened it.
                                transformOrigin =
                                    TransformOrigin(if (alignedToEnd) 1f else 0f, if (opensUpward) 1f else 0f)
                            }.width(IntrinsicSize.Max)
                            .heightIn(max = 520.dp)
                            .liquidGlass(host.frozen, GlassMenuShape, interactive = false)
                            .clip(GlassMenuShape)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                    content = menu.content,
                )
            },
        ) { measurables, constraints ->
            val placeable = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
            val anchorLeft = (menu.anchor.left - origin.x).roundToInt()
            val anchorRight = (menu.anchor.right - origin.x).roundToInt()
            val anchorTop = (menu.anchor.top - origin.y).roundToInt()
            val anchorBottom = (menu.anchor.bottom - origin.y).roundToInt()
            val offX = menu.offset.x.roundToPx()
            val offY = menu.offset.y.roundToPx()
            val gap = 6.dp.roundToPx()
            // Dropdown placement: start-aligned with the anchor when it fits, else end-aligned (the
            // ⋮ at the right edge); below it unless that runs off the bottom.
            val endAligned = anchorLeft + placeable.width > constraints.maxWidth
            val x =
                (if (endAligned) anchorRight - placeable.width - offX else anchorLeft + offX)
                    .coerceIn(0, (constraints.maxWidth - placeable.width).coerceAtLeast(0))
            val below = anchorBottom + offY + gap
            val upward = below + placeable.height > constraints.maxHeight
            val y =
                if (upward) {
                    (anchorTop - placeable.height - offY - gap).coerceAtLeast(0)
                } else {
                    below
                }
            alignedToEnd = endAligned
            opensUpward = upward
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(x, y)
            }
        }
    }
}
