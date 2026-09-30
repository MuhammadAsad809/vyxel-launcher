package com.vyxel.launcher.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import com.vyxel.launcher.core.model.HomePage
import com.vyxel.launcher.core.model.ItemType
import com.vyxel.launcher.core.model.LauncherSettings
import com.vyxel.launcher.core.model.PlacedItem
import com.vyxel.launcher.ui.LauncherViewModel
import com.vyxel.launcher.ui.components.AppIcon
import com.vyxel.launcher.ui.components.ClockFace
import com.vyxel.launcher.ui.components.FolderGlyph
import com.vyxel.launcher.ui.theme.GlassSurface
import com.vyxel.launcher.ui.widgets.BoundWidget

@Composable
fun HomePageGrid(
    page: HomePage,
    settings: LauncherSettings,
    vm: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Layout(
            content = { page.items.forEach { GridCell(it, settings, vm) } }
        ) { measurables, constraints ->
            val cols = settings.gridColumns.coerceAtLeast(1)
            val rows = settings.gridRows.coerceAtLeast(1)
            val cellW = (constraints.maxWidth / cols).coerceAtLeast(1)
            val cellH = (constraints.maxHeight / rows).coerceAtLeast(1)
            val placeables = measurables.mapIndexed { index, measurable ->
                val item = page.items.getOrNull(index)
                val maxW = if (item == null) cellW else (cellW * item.spanX).coerceAtLeast(1)
                val maxH = if (item == null) cellH else (cellH * item.spanY).coerceAtLeast(1)
                val placeable = measurable.measure(
                    constraints.copy(minWidth = 0, minHeight = 0, maxWidth = maxW, maxHeight = maxH)
                )
                val x = (item?.cellX ?: 0) * cellW
                val y = (item?.cellY ?: 0) * cellH
                Triple(placeable, x, y)
            }
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEach { (placeable, x, y) -> placeable.placeRelative(x, y) }
            }
        }
    }
}

@Composable
private fun GridCell(item: PlacedItem, settings: LauncherSettings, vm: LauncherViewModel) {
    when (item.type) {
        ItemType.APP -> {
            val app = vm.app(item.component)
            if (app == null) {
                Box(Modifier.size(settings.iconSizeDp.dp))
            } else {
                AppIcon(
                    app = app,
                    settings = settings,
                    icons = vm.icons(),
                    showLabel = settings.labelsOnHome,
                    onClick = { if (vm.editMode.value) vm.removeFromHome(item.id) else vm.launch(app) },
                    onLongClick = {
                        if (vm.editMode.value) vm.removeFromHome(item.id) else vm.openMenu(app)
                    }
                )
            }
        }
        ItemType.FOLDER -> {
            val apps = item.folderApps.mapNotNull { vm.app(it) }
            FolderGlyph(
                apps = apps,
                settings = settings,
                icons = vm.icons(),
                size = settings.iconSizeDp.dp,
                name = item.folderName ?: "Folder",
                showLabel = settings.labelsOnHome,
                onClick = { vm.openFolder(item) },
                onLongClick = { vm.removeFromHome(item.id) }
            )
        }
        ItemType.CLOCK -> {
            GlassSurface(settings = settings, radius = 24.dp, modifier = Modifier.padding(6.dp)) {
                ClockFace(
                    style = item.clockStyle,
                    weather = null,
                    showWeather = false,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        ItemType.WEATHER -> {
            GlassSurface(settings = settings, radius = 22.dp, modifier = Modifier.padding(6.dp)) {
                ClockFace(
                    style = com.vyxel.launcher.core.model.ClockStyle.MINIMAL,
                    weather = vm.weather.value,
                    showWeather = true,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        ItemType.WIDGET -> {
            BoundWidget(
                id = item.appWidgetId,
                host = vm.widgets(),
                modifier = Modifier.fillMaxSize().padding(4.dp)
            )
        }
    }
}
