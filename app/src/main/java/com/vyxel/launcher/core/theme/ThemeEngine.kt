package com.vyxel.launcher.core.theme

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.vyxel.launcher.core.model.LauncherSettings
import com.vyxel.launcher.core.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ThemeSnapshot(
    val isDark: Boolean,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val muted: Color,
    val glass: Color
)

class ThemeEngine(private val context: Context) {
    fun resolve(settings: LauncherSettings, systemDark: Boolean, wallpaper: Bitmap?): ThemeSnapshot {
        val dark = when (settings.themeMode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM, ThemeMode.DYNAMIC -> systemDark
        }
        var accent = Color(settings.accentArgb.toInt())
        if (settings.useWallpaperPalette && wallpaper != null) {
            val palette = Palette.from(wallpaper).clearFilters().generate()
            val swatch = if (dark) palette.vibrantSwatch ?: palette.lightVibrantSwatch else palette.mutedSwatch
            swatch?.rgb?.let { accent = Color(it) }
        }
        val background = if (dark) Color(0xFF05070C) else Color(0xFFF2F2F7)
        val surface = if (dark) Color(0xFF12151C) else Color(0xFFFFFFFF)
        val onSurface = if (dark) Color.White else Color(0xFF1C1C1E)
        val muted = if (dark) Color(0xFFB0B8C8) else Color(0xFF636366)
        val glass = if (dark) Color.White.copy(alpha = settings.glassOpacity) else Color.White.copy(alpha = 0.55f)
        return ThemeSnapshot(dark, accent, background, surface, onSurface, muted, glass)
    }

    suspend fun paletteAccent(bitmap: Bitmap, dark: Boolean): Color = withContext(Dispatchers.Default) {
        val palette = Palette.from(bitmap).clearFilters().generate()
        val rgb = if (dark) {
            palette.vibrantSwatch?.rgb ?: palette.darkVibrantSwatch?.rgb
        } else {
            palette.lightMutedSwatch?.rgb ?: palette.mutedSwatch?.rgb
        }
        Color(rgb ?: 0xFF0A84FF.toInt())
    }
}

fun fontFamilyOf(name: String) = when (name.lowercase()) {
    "serif" -> androidx.compose.ui.text.font.FontFamily.Serif
    "mono", "monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
    "cursive" -> androidx.compose.ui.text.font.FontFamily.Cursive
    else -> androidx.compose.ui.text.font.FontFamily.SansSerif
}
