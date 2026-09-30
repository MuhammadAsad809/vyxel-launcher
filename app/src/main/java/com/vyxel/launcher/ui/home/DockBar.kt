package com.vyxel.launcher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vyxel.launcher.core.model.LauncherSettings
import com.vyxel.launcher.core.model.PlacedItem
import com.vyxel.launcher.ui.LauncherViewModel
import com.vyxel.launcher.ui.components.AppIcon
import com.vyxel.launcher.ui.theme.GlassSurface

@Composable
fun DockBar(
    items: List<PlacedItem>,
    settings: LauncherSettings,
    vm: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val inner: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.take(settings.dockIconCount).forEach { item ->
                val app = vm.app(item.component) ?: return@forEach
                AppIcon(
                    app = app,
                    settings = settings,
                    icons = vm.icons(),
                    showLabel = settings.dockShowLabels,
                    onClick = { if (vm.editMode.value) vm.removeFromDock(item.id) else vm.launch(app) },
                    onLongClick = { vm.openMenu(app) }
                )
            }
        }
    }
    if (settings.dockBackground) {
        GlassSurface(modifier = modifier.fillMaxWidth(), settings = settings, radius = 34.dp) { inner() }
    } else {
        androidx.compose.foundation.layout.Box(modifier.fillMaxWidth()) { inner() }
    }
}
