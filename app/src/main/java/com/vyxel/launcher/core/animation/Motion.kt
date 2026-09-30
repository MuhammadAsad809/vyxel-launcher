package com.vyxel.launcher.core.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.vyxel.launcher.core.model.AnimationStyle
import com.vyxel.launcher.core.model.LauncherSettings

object Motion {
    fun <T> spec(settings: LauncherSettings) = when (settings.animationStyle) {
        AnimationStyle.SPRING -> spring<T>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow * settings.animationSpeed.coerceIn(0.4f, 2f)
        )
        AnimationStyle.FADE -> tween<T>(durationMillis = (280 / settings.animationSpeed).toInt().coerceIn(80, 800))
        AnimationStyle.SLIDE -> tween<T>(durationMillis = (320 / settings.animationSpeed).toInt().coerceIn(80, 800))
        AnimationStyle.SCALE -> spring<T>(
            dampingRatio = 0.78f,
            stiffness = 380f * settings.animationSpeed.coerceIn(0.4f, 2f)
        )
    }

    fun duration(settings: LauncherSettings, base: Int = 280): Int =
        (base / settings.animationSpeed.coerceIn(0.4f, 2f)).toInt().coerceIn(80, 900)
}
