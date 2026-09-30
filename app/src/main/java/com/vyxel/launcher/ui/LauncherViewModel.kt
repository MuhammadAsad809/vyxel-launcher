package com.vyxel.launcher.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vyxel.launcher.AppContainer
import com.vyxel.launcher.VyxelApplication
import com.vyxel.launcher.core.model.GestureAction
import com.vyxel.launcher.core.model.GestureBinding
import com.vyxel.launcher.core.model.GestureMap
import com.vyxel.launcher.core.model.GestureTrigger
import com.vyxel.launcher.core.model.HomePage
import com.vyxel.launcher.core.model.IconPackInfo
import com.vyxel.launcher.core.model.ItemType
import com.vyxel.launcher.core.model.LaunchApp
import com.vyxel.launcher.core.model.LauncherSettings
import com.vyxel.launcher.core.model.PlacedItem
import com.vyxel.launcher.core.model.WeatherSnapshot
import com.vyxel.launcher.core.notifications.NotificationMirrorService
import com.vyxel.launcher.receivers.LockAdminReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class Overlay { NONE, DRAWER, SEARCH, NOTIFICATIONS, SETTINGS, WIDGETS, FOLDER, MENU }
enum class SettingsPage {
    ROOT, HOME, DOCK, DRAWER, ICONS, THEME, WALLPAPER, GESTURES,
    ANIMATION, GLASS, SEARCH, HIDDEN, CLOCK, BACKUP, ABOUT
}

class LauncherViewModel(private val c: AppContainer) : ViewModel() {
    val settings: StateFlow<LauncherSettings> = c.settings.settings
    val layout = c.layout.layout
    val apps = c.apps.apps
    val notifications = NotificationMirrorService.notifications

    private val _overlay = MutableStateFlow(Overlay.NONE)
    val overlay = _overlay.asStateFlow()

    private val _page = MutableStateFlow(0)
    val page = _page.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _folder = MutableStateFlow<PlacedItem?>(null)
    val folder = _folder.asStateFlow()

    private val _menuApp = MutableStateFlow<LaunchApp?>(null)
    val menuApp = _menuApp.asStateFlow()

    private val _editMode = MutableStateFlow(false)
    val editMode = _editMode.asStateFlow()

    private val _settingsPage = MutableStateFlow(SettingsPage.ROOT)
    val settingsPage = _settingsPage.asStateFlow()

    private val _weather = MutableStateFlow<WeatherSnapshot?>(null)
    val weather = _weather.asStateFlow()

    private val _wallpaper = MutableStateFlow<Bitmap?>(null)
    val wallpaper = _wallpaper.asStateFlow()

    private val _iconPacks = MutableStateFlow<List<IconPackInfo>>(emptyList())
    val iconPacks = _iconPacks.asStateFlow()

    val searchHits = kotlinx.coroutines.flow.combine(apps, query, settings) { list, q, s ->
        c.search.query(list, q, s.hiddenApps.toSet())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            c.settings.load()
            c.layout.load()
            c.apps.reload()
            c.icons.loadPack(c.settings.settings.value.iconPackPackage)
            seedDock()
            refreshWallpaper()
            refreshWeather()
            _iconPacks.value = c.icons.installedPacks()
        }
    }

    private suspend fun seedDock() {
        val preferred = listOf(
            "com.android.chrome", "com.android.phone", "com.android.mms",
            "com.google.android.gm", "com.whatsapp", "com.android.camera2",
            "com.google.android.googlequicksearchbox", "com.android.vending"
        )
        val all = c.apps.apps.value
        val dock = preferred.mapNotNull { pkg -> all.find { it.packageName == pkg }?.component }
            .ifEmpty { all.take(4).map { it.component } }
        c.layout.ensureSeeded(dock)
    }

    fun visibleApps(): List<LaunchApp> = c.apps.visible(settings.value.hiddenApps)

    fun app(component: String?): LaunchApp? = component?.let { c.apps.find(it) }

    fun setOverlay(value: Overlay) {
        _overlay.value = value
        if (value != Overlay.SEARCH) _query.value = ""
        if (value != Overlay.FOLDER) _folder.value = null
        if (value != Overlay.MENU) _menuApp.value = null
        if (value != Overlay.SETTINGS) _settingsPage.value = SettingsPage.ROOT
    }

    fun closeOverlays() {
        _overlay.value = Overlay.NONE
        _query.value = ""
        _folder.value = null
        _menuApp.value = null
        _editMode.value = false
    }

    fun setPage(index: Int) {
        val last = (layout.value.pages.size - 1).coerceAtLeast(0)
        _page.value = index.coerceIn(0, last)
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setSettingsPage(page: SettingsPage) {
        _settingsPage.value = page
    }

    fun toggleEdit() {
        _editMode.value = !_editMode.value
        if (_editMode.value) _overlay.value = Overlay.NONE
    }

    fun launch(app: LaunchApp) {
        haptic()
        c.apps.launch(app)
        closeOverlays()
    }

    fun launchComponent(component: String) {
        c.apps.launchComponent(component)
        closeOverlays()
    }

    fun openMenu(app: LaunchApp) {
        haptic()
        _menuApp.value = app
        _overlay.value = Overlay.MENU
    }

    fun openFolder(item: PlacedItem) {
        _folder.value = item
        _overlay.value = Overlay.FOLDER
    }

    fun shortcuts() = _menuApp.value?.let { c.shortcuts.shortcutsFor(it) }.orEmpty()

    fun startShortcut(id: String) {
        val app = _menuApp.value ?: return
        c.shortcuts.startShortcut(app, id)
        closeOverlays()
    }

    fun hideApp(component: String) {
        viewModelScope.launch {
            c.settings.update { it.copy(hiddenApps = (it.hiddenApps + component).distinct()) }
        }
        closeOverlays()
    }

    fun unhideApp(component: String) {
        viewModelScope.launch {
            c.settings.update { it.copy(hiddenApps = it.hiddenApps - component) }
        }
    }

    fun uninstall(component: String) = c.apps.uninstall(component)
    fun appInfo(component: String) = c.apps.appInfo(component)

    fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        viewModelScope.launch {
            val before = c.settings.settings.value.iconPackPackage
            c.settings.update(transform)
            val after = c.settings.settings.value.iconPackPackage
            if (before != after) {
                c.icons.loadPack(after)
                c.icons.clear()
            }
        }
    }

    fun setCustomIcon(component: String, drawable: String?) {
        viewModelScope.launch {
            c.settings.update {
                val map = it.customIcons.toMutableMap()
                if (drawable.isNullOrBlank()) map.remove(component) else map[component] = drawable
                it.copy(customIcons = map)
            }
            c.icons.clear()
        }
    }

    fun setGesture(trigger: GestureTrigger, action: GestureAction, app: String? = null) {
        viewModelScope.launch {
            c.settings.update { current ->
                val rest = current.gestures.bindings.filterNot { it.trigger == trigger }
                current.copy(gestures = GestureMap(rest + GestureBinding(trigger, action, app)))
            }
        }
    }

    fun perform(action: GestureAction, context: Context) {
        when (action) {
            GestureAction.NONE -> Unit
            GestureAction.OPEN_DRAWER -> setOverlay(Overlay.DRAWER)
            GestureAction.OPEN_SEARCH -> setOverlay(Overlay.SEARCH)
            GestureAction.OPEN_NOTIFICATIONS -> setOverlay(Overlay.NOTIFICATIONS)
            GestureAction.OPEN_SETTINGS -> setOverlay(Overlay.SETTINGS)
            GestureAction.LOCK_SCREEN -> lock(context)
            GestureAction.NEXT_PAGE -> setPage(_page.value + 1)
            GestureAction.PREV_PAGE -> setPage(_page.value - 1)
            GestureAction.SHOW_RECENT_APPS -> setOverlay(Overlay.DRAWER)
            GestureAction.OPEN_APP -> Unit
        }
    }

    fun handleTrigger(trigger: GestureTrigger, context: Context) {
        val binding = settings.value.gestures.actionFor(trigger)
        if (binding.action == GestureAction.OPEN_APP && binding.appComponent != null) {
            launchComponent(binding.appComponent)
        } else {
            perform(binding.action, context)
        }
    }

    fun lock(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, LockAdminReceiver::class.java)
        if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
        } else {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Allow Vyxel to lock the screen from a gesture.")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun addAppToHome(component: String, pageIndex: Int = _page.value) {
        viewModelScope.launch {
            c.layout.update { current ->
                val pages = current.pages.toMutableList()
                val idx = pageIndex.coerceIn(0, pages.lastIndex.coerceAtLeast(0))
                val page = pages.getOrNull(idx) ?: HomePage()
                val gap = firstGap(page, settings.value.gridColumns, settings.value.gridRows)
                    ?: return@update current
                val item = PlacedItem(
                    id = UUID.randomUUID().toString(),
                    type = ItemType.APP,
                    cellX = gap.first,
                    cellY = gap.second,
                    component = component
                )
                pages[idx] = page.copy(items = page.items + item)
                current.copy(pages = pages)
            }
        }
        closeOverlays()
    }

    fun addAppToDock(component: String) {
        viewModelScope.launch {
            c.layout.update { current ->
                if (current.dock.size >= settings.value.dockIconCount) return@update current
                if (current.dock.any { it.component == component }) return@update current
                current.copy(
                    dock = current.dock + PlacedItem(
                        type = ItemType.APP,
                        cellX = current.dock.size,
                        component = component
                    )
                )
            }
        }
        closeOverlays()
    }

    fun removeFromHome(id: String) {
        viewModelScope.launch {
            c.layout.update { current ->
                current.copy(pages = current.pages.map { p -> p.copy(items = p.items.filterNot { it.id == id }) })
            }
            c.layout.removeEmptyPages()
        }
    }

    fun removeFromDock(id: String) {
        viewModelScope.launch {
            c.layout.update { current ->
                current.copy(dock = current.dock.filterNot { it.id == id }.mapIndexed { i, item -> item.copy(cellX = i) })
            }
        }
    }

    fun moveItem(id: String, pageIndex: Int, x: Int, y: Int) {
        viewModelScope.launch {
            c.layout.update { current ->
                val found = current.pages.flatMap { it.items }.find { it.id == id } ?: return@update current
                val cols = settings.value.gridColumns
                val rows = settings.value.gridRows
                if (!inBounds(x, y, found.spanX, found.spanY, cols, rows)) return@update current
                val pages = current.pages.map { p -> p.copy(items = p.items.filterNot { it.id == id }) }.toMutableList()
                while (pages.size <= pageIndex) pages += HomePage()
                val target = pages[pageIndex]
                if (!canPlace(target, x, y, found.spanX, found.spanY, ignoreId = id)) return@update current
                pages[pageIndex] = target.copy(items = target.items + found.copy(cellX = x, cellY = y))
                current.copy(pages = pages)
            }
        }
    }

    fun stackOrMove(dragged: PlacedItem, target: PlacedItem?, pageIndex: Int, x: Int, y: Int) {
        if (target != null && target.id != dragged.id && target.type == ItemType.APP && dragged.type == ItemType.APP) {
            mergeFolder(dragged, target, pageIndex)
        } else {
            moveItem(dragged.id, pageIndex, x, y)
        }
    }

    fun mergeFolder(a: PlacedItem, b: PlacedItem, pageIndex: Int) {
        viewModelScope.launch {
            c.layout.update { current ->
                val pages = current.pages.toMutableList()
                val page = pages.getOrNull(pageIndex) ?: return@update current
                val apps = listOfNotNull(a.component, b.component)
                val folder = PlacedItem(
                    id = b.id,
                    type = ItemType.FOLDER,
                    cellX = b.cellX,
                    cellY = b.cellY,
                    folderName = "Folder",
                    folderApps = apps.distinct()
                )
                pages[pageIndex] = page.copy(items = page.items.filterNot { it.id == a.id || it.id == b.id } + folder)
                current.copy(pages = pages)
            }
        }
    }

    fun addToFolder(folderId: String, component: String) {
        viewModelScope.launch {
            c.layout.update { current ->
                current.copy(
                    pages = current.pages.map { p ->
                        p.copy(
                            items = p.items.map { item ->
                                if (item.id == folderId && component !in item.folderApps) {
                                    item.copy(folderApps = item.folderApps + component)
                                } else item
                            }
                        )
                    }
                )
            }
        }
    }

    fun renameFolder(id: String, name: String) {
        viewModelScope.launch {
            c.layout.update { current ->
                current.copy(
                    pages = current.pages.map { p ->
                        p.copy(items = p.items.map { if (it.id == id) it.copy(folderName = name) else it })
                    }
                )
            }
            _folder.value = _folder.value?.takeIf { it.id == id }?.copy(folderName = name) ?: _folder.value
        }
    }

    fun removeFromFolder(folderId: String, component: String) {
        viewModelScope.launch {
            c.layout.update { current ->
                current.copy(
                    pages = current.pages.map { p ->
                        p.copy(
                            items = p.items.map { item ->
                                if (item.id == folderId) item.copy(folderApps = item.folderApps - component) else item
                            }.filterNot { it.type == ItemType.FOLDER && it.folderApps.isEmpty() }
                        )
                    }
                )
            }
        }
    }

    fun addPage() {
        viewModelScope.launch {
            c.layout.addPage()
            setPage(layout.value.pages.lastIndex)
        }
    }

    fun addClockWidget() {
        placeSpecial(ItemType.CLOCK, spanX = settings.value.gridColumns, spanY = 2)
    }

    fun addWeatherWidget() {
        placeSpecial(ItemType.WEATHER, spanX = settings.value.gridColumns, spanY = 1)
    }

    private fun placeSpecial(type: ItemType, spanX: Int, spanY: Int) {
        viewModelScope.launch {
            c.layout.update { current ->
                val pages = current.pages.toMutableList()
                val idx = _page.value.coerceIn(0, pages.lastIndex)
                val page = pages[idx]
                val gap = firstGap(page, settings.value.gridColumns, settings.value.gridRows, spanX, spanY)
                    ?: return@update current
                pages[idx] = page.copy(
                    items = page.items + PlacedItem(
                        type = type,
                        cellX = gap.first,
                        cellY = gap.second,
                        spanX = spanX,
                        spanY = spanY
                    )
                )
                current.copy(pages = pages)
            }
        }
    }

    fun attachWidget(appWidgetId: Int, provider: String, spanX: Int, spanY: Int) {
        viewModelScope.launch {
            c.layout.update { current ->
                val pages = current.pages.toMutableList()
                val idx = _page.value.coerceIn(0, pages.lastIndex)
                val page = pages[idx]
                val gap = firstGap(page, settings.value.gridColumns, settings.value.gridRows, spanX, spanY)
                    ?: return@update current
                pages[idx] = page.copy(
                    items = page.items + PlacedItem(
                        type = ItemType.WIDGET,
                        cellX = gap.first,
                        cellY = gap.second,
                        spanX = spanX,
                        spanY = spanY,
                        appWidgetId = appWidgetId,
                        widgetProvider = provider
                    )
                )
                current.copy(pages = pages)
            }
        }
        setOverlay(Overlay.NONE)
    }

    fun widgetProviders() = c.widgets.providers()
    fun widgets() = c.widgets
    fun icons() = c.icons
    fun backup() = c.backup

    fun importWallpaper(uri: Uri) {
        viewModelScope.launch {
            if (c.wallpaper.importFrom(uri)) {
                c.settings.update { it.copy(wallpaperUri = uri.toString()) }
                refreshWallpaper()
            }
        }
    }

    fun clearWallpaper() {
        c.wallpaper.clearCustom()
        viewModelScope.launch {
            c.settings.update { it.copy(wallpaperUri = null) }
            refreshWallpaper()
        }
    }

    fun applySystemWallpaper() {
        viewModelScope.launch { c.wallpaper.applySystemWallpaper() }
    }

    fun refreshWallpaper() {
        viewModelScope.launch { _wallpaper.value = c.wallpaper.currentBitmap() }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _weather.value = c.weather.fetch(settings.value.weatherCity)
        }
    }

    fun reloadApps() {
        viewModelScope.launch { c.apps.reload() }
    }

    fun exportLayout(uri: Uri) {
        viewModelScope.launch { c.backup.exportTo(uri) }
    }

    fun importLayout(uri: Uri) {
        viewModelScope.launch {
            c.backup.importFrom(uri)
            c.icons.loadPack(c.settings.settings.value.iconPackPackage)
            refreshWallpaper()
            refreshWeather()
        }
    }

    fun resetLayout() {
        viewModelScope.launch {
            c.layout.replace(com.vyxel.launcher.core.model.HomeLayout())
            seedDock()
        }
    }

    private fun haptic() {
        if (!settings.value.hapticEnabled) return
        val context = VyxelApplication.instance
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(18)
        }
    }

    private fun firstGap(
        page: HomePage,
        cols: Int,
        rows: Int,
        spanX: Int = 1,
        spanY: Int = 1
    ): Pair<Int, Int>? {
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                if (canPlace(page, x, y, spanX, spanY, ignoreId = null) && inBounds(x, y, spanX, spanY, cols, rows)) {
                    return x to y
                }
            }
        }
        return null
    }

    private fun canPlace(
        page: HomePage,
        x: Int,
        y: Int,
        spanX: Int,
        spanY: Int,
        ignoreId: String?
    ): Boolean {
        val cols = settings.value.gridColumns
        val rows = settings.value.gridRows
        if (!inBounds(x, y, spanX, spanY, cols, rows)) return false
        page.items.filter { it.id != ignoreId }.forEach { item ->
            val overlap = x < item.cellX + item.spanX &&
                x + spanX > item.cellX &&
                y < item.cellY + item.spanY &&
                y + spanY > item.cellY
            if (overlap) return false
        }
        return true
    }

    private fun inBounds(x: Int, y: Int, spanX: Int, spanY: Int, cols: Int, rows: Int) =
        x >= 0 && y >= 0 && x + spanX <= cols && y + spanY <= rows

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LauncherViewModel(VyxelApplication.instance.container) as T
        }
    }
}
