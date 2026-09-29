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
val SimpIcons.Bedtime: ImageVector
  get() {
    if (_Bedtime != null) {
      return _Bedtime!!
    }
    _Bedtime =
      ImageVector.Builder(
          name = "Bedtime",
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
            moveTo(12.1f, 22f)
            quadTo(10f, 22f, 8.16f, 21.2f)
            quadTo(6.33f, 20.4f, 4.96f, 19.04f)
            reflectiveQuadTo(2.8f, 15.84f)
            reflectiveQuadTo(2f, 11.9f)
            quadTo(2f, 8.7f, 3.8f, 6.1f)
            reflectiveQuadTo(8.63f, 2.45f)
            quadTo(9.18f, 2.25f, 9.65f, 2.59f)
            reflectiveQuadTo(10.1f, 3.5f)
            quadToRelative(-0.08f, 2.13f, 0.67f, 4.05f)
            reflectiveQuadToRelative(2.25f, 3.42f)
            reflectiveQuadToRelative(3.42f, 2.25f)
            reflectiveQuadTo(20.5f, 13.9f)
            quadToRelative(0.65f, -0.02f, 0.96f, 0.44f)
            reflectiveQuadToRelative(0.11f, 1.04f)
            quadToRelative(-1.1f, 3f, -3.69f, 4.81f)
            reflectiveQuadTo(12.1f, 22f)
            close()
          }
        }
        .build()
    return _Bedtime!!
  }

private var _Bedtime: ImageVector? = null
