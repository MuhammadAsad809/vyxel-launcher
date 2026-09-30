package com.vyxel.launcher.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vyxel.launcher.core.model.LauncherSettings

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    settings: LauncherSettings,
    radius: Dp = 28.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    val fill = Color.White.copy(alpha = settings.glassOpacity.coerceIn(0.08f, 0.55f))
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = settings.glassOpacity + 0.08f),
                        fill,
                        Color.White.copy(alpha = (settings.glassOpacity - 0.04f).coerceAtLeast(0.06f))
                    )
                )
            )
            .border(0.7.dp, Color.White.copy(alpha = 0.32f), shape)
    ) {
        if (settings.blurEnabled && Build.VERSION.SDK_INT >= 31) {
            Box(
                Modifier
                    .matchParentSize()
                    .blur(settings.blurRadiusDp.dp.coerceIn(8.dp, 48.dp))
                    .background(Color.White.copy(alpha = 0.04f))
            )
        }
        content()
    }
}
