package com.maxrave.simpmusic.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
val SimpIcons.Diamond: ImageVector
  get() {
    if (_Diamond != null) {
      return _Diamond!!
    }
    _Diamond =
      ImageVector.Builder(
          name = "Diamond",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(9.2f, 8.25f)
            horizontalLineToRelative(5.6f)
            lineTo(12.15f, 3f)
            horizontalLineToRelative(-0.3f)
            lineTo(9.2f, 8.25f)
            close()
            moveTo(11.25f, 20.1f)
            verticalLineTo(9.75f)
            horizontalLineTo(2.63f)
            lineTo(11.25f, 20.1f)
            close()
            moveToRelative(1.5f, 0f)
            lineTo(21.38f, 9.75f)
            horizontalLineTo(12.75f)
            verticalLineTo(20.1f)
            close()
            moveTo(16.45f, 8.25f)
            horizontalLineToRelative(5.18f)
            lineTo(19.55f, 4.1f)
            quadTo(19.28f, 3.6f, 18.81f, 3.3f)
            reflectiveQuadTo(17.78f, 3f)
            horizontalLineTo(13.85f)
            lineToRelative(2.6f, 5.25f)
            close()
            moveToRelative(-14.08f, 0f)
            horizontalLineTo(7.55f)
            lineTo(10.15f, 3f)
            horizontalLineTo(6.23f)
            quadTo(5.65f, 3f, 5.19f, 3.3f)
            quadTo(4.73f, 3.6f, 4.45f, 4.1f)
            lineTo(2.38f, 8.25f)
            close()
          }
        }
        .build()
    return _Diamond!!
  }

private var _Diamond: ImageVector? = null
