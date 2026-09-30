package com.vyxel.launcher.ui.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyxel.launcher.core.model.LaunchApp
import com.vyxel.launcher.ui.LauncherViewModel
import com.vyxel.launcher.ui.components.AppIcon
import com.vyxel.launcher.ui.theme.GlassSurface
import com.vyxel.launcher.ui.theme.LocalVyxelFont

@Composable
fun AppContextMenu(app: LaunchApp, vm: LauncherViewModel, onDismiss: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val shortcuts = remember(app.component) { vm.shortcuts() }
    GlassSurface(
        settings = settings,
        radius = 26.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp)
    ) {
        Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AppIcon(app, settings, vm.icons(), showLabel = true, onClick = { vm.launch(app) })
            Spacer(Modifier.height(12.dp))
            shortcuts.forEach { s ->
                MenuRow(s.label.ifBlank { "Shortcut" }) { vm.startShortcut(s.id) }
            }
            MenuRow("Add to Home") { vm.addAppToHome(app.component) }
            MenuRow("Add to Dock") { vm.addAppToDock(app.component) }
            MenuRow("Reset custom icon") { vm.setCustomIcon(app.component, null); onDismiss() }
            MenuRow("App info") { vm.appInfo(app.component); onDismiss() }
            MenuRow("Hide app") { vm.hideApp(app.component) }
            MenuRow("Uninstall", Color(0xFFFF453A)) { vm.uninstall(app.component); onDismiss() }
            MenuRow("Close") { onDismiss() }
        }
    }
}

@Composable
private fun MenuRow(label: String, color: Color = Color.White, onClick: () -> Unit) {
    Text(
        label,
        color = color,
        fontSize = 16.sp,
        fontFamily = LocalVyxelFont.current,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    )
}
