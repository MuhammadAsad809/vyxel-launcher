package com.vyxel.launcher.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyxel.launcher.ui.LauncherViewModel
import com.vyxel.launcher.ui.components.AppIcon
import com.vyxel.launcher.ui.theme.GlassSurface
import com.vyxel.launcher.ui.theme.LocalVyxelFont

@Composable
fun Spotlight(vm: LauncherViewModel, onClose: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val query by vm.query.collectAsState()
    val hits by vm.searchHits.collectAsState()
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f))
            .clickable(onClick = onClose)
            .statusBarsPadding()
            .padding(18.dp)
    ) {
        GlassSurface(
            settings = settings,
            radius = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = query,
                    onValueChange = vm::setQuery,
                    singleLine = true,
                    cursorBrush = SolidColor(Color.White),
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontFamily = LocalVyxelFont.current),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focus),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text("Spotlight search", color = Color.White.copy(alpha = 0.4f))
                        inner()
                    }
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn {
            items(hits, key = { it.app.component }) { hit ->
                GlassSurface(
                    settings = settings,
                    radius = 18.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { vm.launch(hit.app) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            app = hit.app,
                            settings = settings,
                            icons = vm.icons(),
                            showLabel = false,
                            size = 40.dp,
                            onClick = { vm.launch(hit.app) }
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(hit.app.label, color = Color.White, fontSize = 16.sp, fontFamily = LocalVyxelFont.current)
                            Text(hit.app.packageName, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
