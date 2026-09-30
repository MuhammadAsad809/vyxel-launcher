package com.vyxel.launcher.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyxel.launcher.core.model.AnimationStyle
import com.vyxel.launcher.core.model.ClockStyle
import com.vyxel.launcher.core.model.DrawerStyle
import com.vyxel.launcher.core.model.GestureAction
import com.vyxel.launcher.core.model.GestureTrigger
import com.vyxel.launcher.core.model.IconShape
import com.vyxel.launcher.core.model.LauncherSettings
import com.vyxel.launcher.core.model.ThemeMode
import com.vyxel.launcher.ui.LauncherViewModel
import com.vyxel.launcher.ui.SettingsPage
import com.vyxel.launcher.ui.theme.GlassSurface
import com.vyxel.launcher.ui.theme.LocalVyxelFont

@Composable
fun SettingsScreen(vm: LauncherViewModel, onClose: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val page by vm.settingsPage.collectAsState()
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Header(settings, page, vm, onClose)
        Spacer(Modifier.height(12.dp))
        Column(Modifier.verticalScroll(rememberScrollState())) {
            when (page) {
                SettingsPage.ROOT -> RootMenu(vm)
                SettingsPage.HOME -> HomeSection(vm, settings)
                SettingsPage.DOCK -> DockSection(vm, settings)
                SettingsPage.DRAWER -> DrawerSection(vm, settings)
                SettingsPage.ICONS -> IconsSection(vm, settings)
                SettingsPage.THEME -> ThemeSection(vm, settings)
                SettingsPage.WALLPAPER -> WallpaperSection(vm, settings)
                SettingsPage.GESTURES -> GestureSection(vm, settings)
                SettingsPage.ANIMATION -> AnimationSection(vm, settings)
                SettingsPage.GLASS -> GlassSection(vm, settings)
                SettingsPage.SEARCH -> SearchSection(vm, settings)
                SettingsPage.HIDDEN -> HiddenSection(vm, settings)
                SettingsPage.CLOCK -> ClockSection(vm, settings)
                SettingsPage.BACKUP -> BackupSection(vm)
                SettingsPage.ABOUT -> AboutSection()
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Header(settings: LauncherSettings, page: SettingsPage, vm: LauncherViewModel, onClose: () -> Unit) {
    GlassSurface(settings = settings, radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (page == SettingsPage.ROOT) "Settings" else page.name.lowercase().replaceFirstChar { it.titlecase() },
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = LocalVyxelFont.current,
                modifier = Modifier.weight(1f)
            )
            if (page != SettingsPage.ROOT) {
                Text("Back", color = Color(0xFF6BA3F5), modifier = Modifier.clickable { vm.setSettingsPage(SettingsPage.ROOT) }.padding(8.dp))
            }
            Text("Done", color = Color(0xFF6BA3F5), modifier = Modifier.clickable(onClick = onClose).padding(8.dp))
        }
    }
}

@Composable
private fun RootMenu(vm: LauncherViewModel) {
    listOf(
        SettingsPage.HOME to "Home screen & grid",
        SettingsPage.DOCK to "Dock",
        SettingsPage.DRAWER to "App drawer",
        SettingsPage.ICONS to "Icons & packs",
        SettingsPage.THEME to "Theme & fonts",
        SettingsPage.WALLPAPER to "Wallpaper engine",
        SettingsPage.GESTURES to "Gestures",
        SettingsPage.ANIMATION to "Animation engine",
        SettingsPage.GLASS to "Blur & glass",
        SettingsPage.SEARCH to "Universal search",
        SettingsPage.HIDDEN to "Hidden apps",
        SettingsPage.CLOCK to "Clock & weather",
        SettingsPage.BACKUP to "Backup & layouts",
        SettingsPage.ABOUT to "About"
    ).forEach { (page, label) -> NavRow(label) { vm.setSettingsPage(page) } }
}

@Composable
private fun HomeSection(vm: LauncherViewModel, s: LauncherSettings) {
    IntRow("Columns", s.gridColumns, 3, 6) { v -> vm.updateSettings { it.copy(gridColumns = v) } }
    IntRow("Rows", s.gridRows, 4, 8) { v -> vm.updateSettings { it.copy(gridRows = v) } }
    Toggle("Home labels", s.labelsOnHome) { vm.updateSettings { it.copy(labelsOnHome = !s.labelsOnHome) } }
    Toggle("Haptics", s.hapticEnabled) { vm.updateSettings { it.copy(hapticEnabled = !s.hapticEnabled) } }
    Toggle("Page wrap hint", s.infiniteScroll) { vm.updateSettings { it.copy(infiniteScroll = !s.infiniteScroll) } }
    NavRow("Add home page") { vm.addPage() }
}

@Composable
private fun DockSection(vm: LauncherViewModel, s: LauncherSettings) {
    IntRow("Dock icons", s.dockIconCount, 3, 7) { v -> vm.updateSettings { it.copy(dockIconCount = v) } }
    Toggle("Dock labels", s.dockShowLabels) { vm.updateSettings { it.copy(dockShowLabels = !s.dockShowLabels) } }
    Toggle("Glass dock", s.dockBackground) { vm.updateSettings { it.copy(dockBackground = !s.dockBackground) } }
}

@Composable
private fun DrawerSection(vm: LauncherViewModel, s: LauncherSettings) {
    Label("Style")
    ChipRow(DrawerStyle.entries.map { it.name.lowercase() to (s.drawerStyle == it) }) { index ->
        vm.updateSettings { it.copy(drawerStyle = DrawerStyle.entries[index]) }
    }
    IntRow("Drawer columns", s.drawerColumns, 3, 6) { v -> vm.updateSettings { it.copy(drawerColumns = v) } }
    Toggle("Drawer labels", s.labelsOnDrawer) { vm.updateSettings { it.copy(labelsOnDrawer = !s.labelsOnDrawer) } }
}

@Composable
private fun IconsSection(vm: LauncherViewModel, s: LauncherSettings) {
    Label("Shape")
    IconShape.entries.chunked(4).forEach { row ->
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { shape ->
                Chip(shape.name.lowercase(), s.iconShape == shape) { vm.updateSettings { it.copy(iconShape = shape) } }
            }
        }
    }
    IntRow("Icon size", s.iconSizeDp, 44, 76) { v -> vm.updateSettings { it.copy(iconSizeDp = v) } }
    Label("Icon packs")
    Chip("System icons", s.iconPackPackage == null) { vm.updateSettings { it.copy(iconPackPackage = null) } }
    val packs by vm.iconPacks.collectAsState()
    packs.forEach { pack ->
        Chip(pack.label, s.iconPackPackage == pack.packageName) {
            vm.updateSettings { it.copy(iconPackPackage = pack.packageName) }
        }
    }
    Text(
        "Per-app icons: icon packs map automatically through appfilter.xml. Overrides live in backup JSON under customIcons.",
        color = Color.White.copy(alpha = 0.55f),
        fontSize = 12.sp,
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
private fun ThemeSection(vm: LauncherViewModel, s: LauncherSettings) {
    ChipRow(ThemeMode.entries.map { it.name.lowercase() to (s.themeMode == it) }) { index ->
        vm.updateSettings { it.copy(themeMode = ThemeMode.entries[index]) }
    }
    Toggle("Wallpaper palette", s.useWallpaperPalette) { vm.updateSettings { it.copy(useWallpaperPalette = !s.useWallpaperPalette) } }
    Label("Font")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("sans", "serif", "mono", "cursive").forEach { font ->
            Chip(font, s.fontFamily == font) { vm.updateSettings { it.copy(fontFamily = font) } }
        }
    }
    Label("Accent")
    val accents = listOf(0xFF0A84FF, 0xFF6BA3F5, 0xFF30D158, 0xFFFF9F0A, 0xFFFF453A, 0xFFBF5AF2)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        accents.forEach { color ->
            Chip("#${color.toString(16).takeLast(6)}", s.accentArgb == color) {
                vm.updateSettings { it.copy(accentArgb = color) }
            }
        }
    }
}

@Composable
private fun WallpaperSection(vm: LauncherViewModel, s: LauncherSettings) {
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importWallpaper(uri)
    }
    NavRow("Choose image") { pick.launch(arrayOf("image/*")) }
    NavRow("Use as system wallpaper") { vm.applySystemWallpaper() }
    NavRow("Clear custom wallpaper") { vm.clearWallpaper() }
    SliderRow("Dim", s.wallpaperDim, 0f, 0.7f) { v -> vm.updateSettings { it.copy(wallpaperDim = v) } }
    Toggle("Parallax", s.wallpaperParallax) { vm.updateSettings { it.copy(wallpaperParallax = !s.wallpaperParallax) } }
}

@Composable
private fun GestureSection(vm: LauncherViewModel, s: LauncherSettings) {
    val actions = listOf(
        GestureAction.NONE, GestureAction.OPEN_DRAWER, GestureAction.OPEN_SEARCH,
        GestureAction.OPEN_NOTIFICATIONS, GestureAction.OPEN_SETTINGS, GestureAction.LOCK_SCREEN
    )
    GestureTrigger.entries.forEach { trigger ->
        val current = s.gestures.actionFor(trigger).action
        Column(Modifier.padding(vertical = 6.dp)) {
            Text(trigger.name.replace('_', ' ').lowercase(), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                actions.forEach { action ->
                    Chip(action.name.lowercase().replace('_', ' '), current == action) {
                        vm.setGesture(trigger, action)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimationSection(vm: LauncherViewModel, s: LauncherSettings) {
    ChipRow(AnimationStyle.entries.map { it.name.lowercase() to (s.animationStyle == it) }) { index ->
        vm.updateSettings { it.copy(animationStyle = AnimationStyle.entries[index]) }
    }
    SliderRow("Speed", s.animationSpeed, 0.5f, 1.8f) { v -> vm.updateSettings { it.copy(animationSpeed = v) } }
}

@Composable
private fun GlassSection(vm: LauncherViewModel, s: LauncherSettings) {
    Toggle("Blur", s.blurEnabled) { vm.updateSettings { it.copy(blurEnabled = !s.blurEnabled) } }
    IntRow("Blur radius", s.blurRadiusDp, 8, 48) { v -> vm.updateSettings { it.copy(blurRadiusDp = v) } }
    SliderRow("Glass opacity", s.glassOpacity, 0.08f, 0.5f) { v -> vm.updateSettings { it.copy(glassOpacity = v) } }
    Toggle("Transparent status", s.statusBarTransparent) { vm.updateSettings { it.copy(statusBarTransparent = !s.statusBarTransparent) } }
}

@Composable
private fun SearchSection(vm: LauncherViewModel, s: LauncherSettings) {
    Toggle("Spotlight search", s.searchEnabled) { vm.updateSettings { it.copy(searchEnabled = !s.searchEnabled) } }
    Text("Swipe down on Home to open universal app search.", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, modifier = Modifier.padding(8.dp))
}

@Composable
private fun HiddenSection(vm: LauncherViewModel, s: LauncherSettings) {
    if (s.hiddenApps.isEmpty()) {
        Text("No hidden apps. Long-press any icon → Hide app.", color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(8.dp))
    }
    s.hiddenApps.forEach { component ->
        val app = vm.app(component)
        NavRow("${app?.label ?: component}  ·  unhide") { vm.unhideApp(component) }
    }
}

@Composable
private fun ClockSection(vm: LauncherViewModel, s: LauncherSettings) {
    val context = LocalContext.current
    Toggle("Show clock", s.showClock) { vm.updateSettings { it.copy(showClock = !s.showClock) } }
    Toggle("Show weather", s.showWeather) { vm.updateSettings { it.copy(showWeather = !s.showWeather) } }
    ChipRow(ClockStyle.entries.map { it.name.lowercase() to (s.clockStyle == it) }) { index ->
        vm.updateSettings { it.copy(clockStyle = ClockStyle.entries[index]) }
    }
    NavRow("Refresh weather") { vm.refreshWeather() }
    NavRow("Location settings") {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

@Composable
private fun BackupSection(vm: LauncherViewModel) {
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.exportLayout(uri)
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importLayout(uri)
    }
    NavRow("Export layout & settings") { create.launch(vm.backup().suggestedFileName()) }
    NavRow("Import layout & settings") { open.launch(arrayOf("application/json", "*/*")) }
    NavRow("Reset home layout") { vm.resetLayout() }
}

@Composable
private fun AboutSection() {
    Text("Vyxel Launcher", color = Color.White, fontSize = 20.sp, fontFamily = LocalVyxelFont.current, modifier = Modifier.padding(8.dp))
    Text(
        "iOS-inspired Liquid Glass home with a full core launcher stack: drawer, search, widgets, gestures, icon packs, theming, wallpaper, folders, backup.",
        color = Color.White.copy(alpha = 0.7f),
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
private fun Label(text: String) {
    Text(text, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(8.dp), fontFamily = LocalVyxelFont.current)
}

@Composable
private fun ChipRow(items: List<Pair<String, Boolean>>, onIndex: (Int) -> Unit) {
    Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { index, (label, selected) ->
            Chip(label, selected) { onIndex(index) }
        }
    }
}

@Composable
private fun NavRow(label: String, onClick: () -> Unit) {
    GlassSurface(settings = LauncherSettings(), radius = 16.dp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable(onClick = onClick)) {
        Text(label, color = Color.White, modifier = Modifier.padding(16.dp), fontFamily = LocalVyxelFont.current)
    }
}

@Composable
private fun Toggle(label: String, value: Boolean, onClick: () -> Unit) {
    GlassSurface(settings = LauncherSettings(), radius = 16.dp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.White, modifier = Modifier.weight(1f), fontFamily = LocalVyxelFont.current)
            Switch(checked = value, onCheckedChange = { onClick() })
        }
    }
}

@Composable
private fun IntRow(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    GlassSurface(settings = LauncherSettings(), radius = 16.dp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$label  $value", color = Color.White, modifier = Modifier.weight(1f), fontFamily = LocalVyxelFont.current)
            Text("−", color = Color(0xFF6BA3F5), modifier = Modifier.clickable { onChange((value - 1).coerceAtLeast(min)) }.padding(10.dp))
            Text("+", color = Color(0xFF6BA3F5), modifier = Modifier.clickable { onChange((value + 1).coerceAtMost(max)) }.padding(10.dp))
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    GlassSurface(settings = LauncherSettings(), radius = 16.dp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("$label  ${"%.2f".format(value)}", color = Color.White, fontFamily = LocalVyxelFont.current)
            Slider(value = value, onValueChange = onChange, valueRange = min..max)
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    GlassSurface(
        settings = LauncherSettings().copy(glassOpacity = if (selected) 0.38f else 0.16f),
        radius = 14.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(label, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), fontSize = 12.sp)
    }
}
