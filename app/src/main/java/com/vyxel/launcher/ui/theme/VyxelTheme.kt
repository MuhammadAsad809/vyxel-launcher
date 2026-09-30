package com.vyxel.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vyxel.launcher.core.theme.ThemeSnapshot
import com.vyxel.launcher.core.theme.fontFamilyOf

val LocalVyxelTheme = staticCompositionLocalOf {
    ThemeSnapshot(
        isDark = true,
        accent = Color(0xFF0A84FF),
        background = Color(0xFF05070C),
        surface = Color(0xFF12151C),
        onSurface = Color.White,
        muted = Color(0xFFB0B8C8),
        glass = Color.White.copy(alpha = 0.22f)
    )
}

val LocalVyxelFont = staticCompositionLocalOf { FontFamily.SansSerif }

@Composable
fun VyxelTheme(
    snapshot: ThemeSnapshot,
    fontName: String,
    content: @Composable () -> Unit
) {
    val font = fontFamilyOf(fontName)
    val scheme = if (snapshot.isDark) {
        darkColorScheme(
            primary = snapshot.accent,
            onPrimary = Color.White,
            background = snapshot.background,
            surface = snapshot.surface,
            onBackground = snapshot.onSurface,
            onSurface = snapshot.onSurface
        )
    } else {
        lightColorScheme(
            primary = snapshot.accent,
            onPrimary = Color.White,
            background = snapshot.background,
            surface = snapshot.surface,
            onBackground = snapshot.onSurface,
            onSurface = snapshot.onSurface
        )
    }
    CompositionLocalProvider(
        LocalVyxelTheme provides snapshot,
        LocalVyxelFont provides font
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = MaterialTheme.typography.copy(
                displayLarge = TextStyle(fontFamily = font, fontWeight = FontWeight.Light, fontSize = 64.sp),
                titleLarge = TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
                bodyLarge = TextStyle(fontFamily = font, fontSize = 16.sp),
                labelSmall = TextStyle(fontFamily = font, fontSize = 11.sp)
            ),
            content = content
        )
    }
}
