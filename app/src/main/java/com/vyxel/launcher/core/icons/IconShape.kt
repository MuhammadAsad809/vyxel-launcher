package com.vyxel.launcher.core.icons

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.vyxel.launcher.core.model.IconShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

fun IconShape.asComposeShape(): Shape = when (this) {
    IconShape.SQUIRCLE -> SuperellipseShape(5f)
    IconShape.CIRCLE -> CircleShape
    IconShape.ROUNDED -> RoundedCornerShape(16.dp)
    IconShape.SQUARE -> RoundedCornerShape(6.dp)
    IconShape.TEARDROP -> TeardropShape
    IconShape.HEXAGON -> PolygonShape(6)
    IconShape.LEAF -> LeafShape
}

class SuperellipseShape(private val n: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val a = size.width / 2f
        val b = size.height / 2f
        val cx = a
        val cy = b
        val steps = 64
        for (i in 0..steps) {
            val t = (i / steps.toFloat()) * (2f * PI.toFloat())
            val cosT = cos(t)
            val sinT = sin(t)
            val x = cx + a * signPow(cosT, 2f / n)
            val y = cy + b * signPow(sinT, 2f / n)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }

    private fun signPow(v: Float, exp: Float): Float {
        val s = if (v < 0f) -1f else 1f
        return s * kotlin.math.abs(v).toDouble().pow(exp.toDouble()).toFloat()
    }
}

object TeardropShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = size.minDimension * 0.28f
        return Outline.Rounded(
            androidx.compose.ui.geometry.RoundRect(
                left = 0f, top = 0f, right = size.width, bottom = size.height,
                topLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension / 2),
                topRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension / 2),
                bottomRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(r),
                bottomLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension / 2)
            )
        )
    }
}

class PolygonShape(private val sides: Int) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f
        for (i in 0 until sides) {
            val a = (i / sides.toFloat()) * (2f * PI.toFloat()) - (PI.toFloat() / 2f)
            val x = cx + r * cos(a)
            val y = cy + r * sin(a)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

object LeafShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val big = size.minDimension / 2f
        val small = size.minDimension * 0.18f
        return Outline.Rounded(
            androidx.compose.ui.geometry.RoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                topLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(big),
                topRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(small),
                bottomRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(big),
                bottomLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(small)
            )
        )
    }
}
