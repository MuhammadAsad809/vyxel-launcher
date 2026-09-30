package com.vyxel.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyxel.launcher.core.model.LauncherSettings
import com.vyxel.launcher.core.model.PlacedItem
import com.vyxel.launcher.ui.LauncherViewModel
import com.vyxel.launcher.ui.components.AppIcon
import com.vyxel.launcher.ui.theme.GlassSurface
import com.vyxel.launcher.ui.theme.LocalVyxelFont

@Composable
fun FolderSheet(
    item: PlacedItem,
    settings: LauncherSettings,
    vm: LauncherViewModel,
    onDismiss: () -> Unit
) {
    var name by remember(item.id) { mutableStateOf(item.folderName ?: "Folder") }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            settings = settings,
            radius = 32.dp
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        vm.renameFolder(item.id, it)
                    },
                    singleLine = true,
                    cursorBrush = SolidColor(Color.White),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = LocalVyxelFont.current
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(item.folderApps, key = { it }) { component ->
                        val app = vm.app(component) ?: return@items
                        AppIcon(
                            app = app,
                            settings = settings,
                            icons = vm.icons(),
                            onClick = { vm.launch(app) },
                            onLongClick = { vm.removeFromFolder(item.id, component) }
                        )
                    }
                }
            }
        }
    }
}
