package com.vyxel.launcher.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class IconShape {
    SQUIRCLE, CIRCLE, ROUNDED, SQUARE, TEARDROP, HEXAGON, LEAF
}

@Serializable
enum class ThemeMode { LIGHT, DARK, SYSTEM, DYNAMIC }

@Serializable
enum class DrawerStyle { VERTICAL, PAGED, CATEGORY }

@Serializable
enum class ClockStyle { IOS, DIGITAL, ANALOG, MINIMAL }

@Serializable
enum class AnimationStyle { SPRING, FADE, SLIDE, SCALE }

@Serializable
enum class ItemType { APP, FOLDER, WIDGET, CLOCK, WEATHER }

@Serializable
enum class GestureAction {
    NONE,
    OPEN_DRAWER,
    OPEN_SEARCH,
    OPEN_NOTIFICATIONS,
    OPEN_SETTINGS,
    LOCK_SCREEN,
    NEXT_PAGE,
    PREV_PAGE,
    SHOW_RECENT_APPS,
    OPEN_APP
}

@Serializable
enum class GestureTrigger {
    SWIPE_UP,
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    SWIPE_DOWN_STATUS,
    DOUBLE_TAP,
    PINCH_IN,
    TWO_FINGER_SWIPE_UP,
    TWO_FINGER_SWIPE_DOWN
}

@Serializable
data class GestureBinding(
    val trigger: GestureTrigger,
    val action: GestureAction,
    val appComponent: String? = null
)

@Serializable
data class GestureMap(
    val bindings: List<GestureBinding> = defaults()
) {
    fun actionFor(trigger: GestureTrigger): GestureBinding =
        bindings.firstOrNull { it.trigger == trigger }
            ?: GestureBinding(trigger, GestureAction.NONE)

    companion object {
        fun defaults() = listOf(
            GestureBinding(GestureTrigger.SWIPE_UP, GestureAction.OPEN_DRAWER),
            GestureBinding(GestureTrigger.SWIPE_DOWN, GestureAction.OPEN_SEARCH),
            GestureBinding(GestureTrigger.SWIPE_DOWN_STATUS, GestureAction.OPEN_NOTIFICATIONS),
            GestureBinding(GestureTrigger.DOUBLE_TAP, GestureAction.LOCK_SCREEN),
            GestureBinding(GestureTrigger.PINCH_IN, GestureAction.OPEN_SETTINGS),
            GestureBinding(GestureTrigger.TWO_FINGER_SWIPE_UP, GestureAction.OPEN_NOTIFICATIONS),
            GestureBinding(GestureTrigger.SWIPE_LEFT, GestureAction.NEXT_PAGE),
            GestureBinding(GestureTrigger.SWIPE_RIGHT, GestureAction.PREV_PAGE)
        )
    }
}

@Serializable
data class LauncherSettings(
    val gridColumns: Int = 4,
    val gridRows: Int = 6,
    val dockColumns: Int = 4,
    val dockShowLabels: Boolean = false,
    val dockBackground: Boolean = true,
    val labelsOnHome: Boolean = true,
    val labelsOnDrawer: Boolean = true,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val iconSizeDp: Int = 58,
    val iconPackPackage: String? = null,
    val customIcons: Map<String, String> = emptyMap(),
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentArgb: Long = 0xFF0A84FF,
    val useWallpaperPalette: Boolean = true,
    val blurEnabled: Boolean = true,
    val blurRadiusDp: Int = 28,
    val glassOpacity: Float = 0.22f,
    val wallpaperDim: Float = 0.28f,
    val wallpaperParallax: Boolean = true,
    val wallpaperUri: String? = null,
    val animationStyle: AnimationStyle = AnimationStyle.SPRING,
    val animationSpeed: Float = 1f,
    val fontFamily: String = "sans",
    val showClock: Boolean = true,
    val showWeather: Boolean = true,
    val clockStyle: ClockStyle = ClockStyle.IOS,
    val weatherCity: String = "",
    val drawerStyle: DrawerStyle = DrawerStyle.VERTICAL,
    val drawerColumns: Int = 4,
    val searchEnabled: Boolean = true,
    val hiddenApps: List<String> = emptyList(),
    val gestures: GestureMap = GestureMap(),
    val hapticEnabled: Boolean = true,
    val infiniteScroll: Boolean = false,
    val statusBarTransparent: Boolean = true,
    val dockIconCount: Int = 4
)

@Serializable
data class PlacedItem(
    val id: String = UUID.randomUUID().toString(),
    val type: ItemType = ItemType.APP,
    val cellX: Int = 0,
    val cellY: Int = 0,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val component: String? = null,
    val folderName: String? = null,
    val folderApps: List<String> = emptyList(),
    val appWidgetId: Int = -1,
    val widgetProvider: String? = null,
    val clockStyle: ClockStyle = ClockStyle.IOS
)

@Serializable
data class HomePage(
    val id: String = UUID.randomUUID().toString(),
    val items: List<PlacedItem> = emptyList()
)

@Serializable
data class HomeLayout(
    val pages: List<HomePage> = listOf(HomePage()),
    val dock: List<PlacedItem> = emptyList()
)

data class LaunchApp(
    val label: String,
    val packageName: String,
    val activityName: String,
    val component: String = "$packageName/$activityName"
)

data class AppShortcut(
    val id: String,
    val label: String,
    val packageName: String,
    val intent: android.content.Intent?
)

data class IconPackInfo(
    val packageName: String,
    val label: String
)

data class SearchHit(
    val app: LaunchApp,
    val score: Int
)

data class WeatherSnapshot(
    val city: String,
    val celsius: Int,
    val condition: String,
    val code: Int
)

data class ShadeNotification(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val time: Long,
    val category: String?
)

data class WidgetProviderInfo(
    val packageName: String,
    val className: String,
    val label: String,
    val minWidth: Int,
    val minHeight: Int,
    val preview: android.graphics.drawable.Drawable?
)
