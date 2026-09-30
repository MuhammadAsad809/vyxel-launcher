package com.vyxel.launcher

import android.content.Context
import com.vyxel.launcher.core.apps.AppRepository
import com.vyxel.launcher.core.apps.ShortcutHelper
import com.vyxel.launcher.core.data.BackupManager
import com.vyxel.launcher.core.data.JsonStore
import com.vyxel.launcher.core.data.LayoutRepository
import com.vyxel.launcher.core.data.SettingsRepository
import com.vyxel.launcher.core.gestures.GestureController
import com.vyxel.launcher.core.icons.IconPackManager
import com.vyxel.launcher.core.search.SearchEngine
import com.vyxel.launcher.core.theme.ThemeEngine
import com.vyxel.launcher.core.wallpaper.WallpaperEngine
import com.vyxel.launcher.core.weather.WeatherRepository
import com.vyxel.launcher.core.widgets.WidgetHostManager

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val store = JsonStore(appContext)
    val settings = SettingsRepository(store)
    val layout = LayoutRepository(store)
    val apps = AppRepository(appContext)
    val shortcuts = ShortcutHelper(appContext)
    val icons = IconPackManager(appContext)
    val theme = ThemeEngine(appContext)
    val gestures = GestureController()
    val wallpaper = WallpaperEngine(appContext)
    val widgets = WidgetHostManager(appContext)
    val search = SearchEngine()
    val weather = WeatherRepository(appContext)
    val backup = BackupManager(appContext, settings, layout)
}
