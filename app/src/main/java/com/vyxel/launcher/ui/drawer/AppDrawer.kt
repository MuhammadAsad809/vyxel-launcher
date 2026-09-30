package com.vyxel.launcher.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyxel.launcher.core.model.DrawerStyle
import com.vyxel.launcher.core.model.LaunchApp
import com.vyxel.launcher.ui.LauncherViewModel
import com.vyxel.launcher.ui.components.AppIcon
import com.vyxel.launcher.ui.theme.GlassSurface
import com.vyxel.launcher.ui.theme.LocalVyxelFont
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(vm: LauncherViewModel, onClose: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val query by vm.query.collectAsState()
    val apps = vm.visibleApps().let { list ->
        if (query.isBlank()) list else list.filter { it.label.contains(query, true) || it.packageName.contains(query, true) }
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.28f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        GlassSurface(settings = settings, radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.padding(6.dp))
                BasicTextField(
                    value = query,
                    onValueChange = vm::setQuery,
                    singleLine = true,
                    cursorBrush = SolidColor(Color.White),
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontFamily = LocalVyxelFont.current),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text("Search apps", color = Color.White.copy(alpha = 0.45f), fontFamily = LocalVyxelFont.current)
                        }
                        inner()
                    }
                )
                Text(
                    "Close",
                    color = Color(0xFF6BA3F5),
                    modifier = Modifier.clickable(onClick = onClose).padding(8.dp),
                    fontFamily = LocalVyxelFont.current
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        when (settings.drawerStyle) {
            DrawerStyle.VERTICAL -> DrawerGrid(apps, vm, settings.drawerColumns)
            DrawerStyle.PAGED -> PagedDrawer(apps, vm, settings.drawerColumns)
            DrawerStyle.CATEGORY -> CategoryDrawer(apps, vm, settings.drawerColumns)
        }
    }
}

@Composable
private fun DrawerGrid(apps: List<LaunchApp>, vm: LauncherViewModel, columns: Int) {
    val settings by vm.settings.collectAsState()
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns.coerceIn(3, 6)),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 32.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(apps, key = { it.component }) { app ->
            AppIcon(
                app = app,
                settings = settings,
                icons = vm.icons(),
                showLabel = settings.labelsOnDrawer,
                onClick = { vm.launch(app) },
                onLongClick = { vm.openMenu(app) }
            )
        }
    }
}

@Composable
private fun PagedDrawer(apps: List<LaunchApp>, vm: LauncherViewModel, columns: Int) {
    val perPage = columns * 5
    val pages = apps.chunked(perPage.coerceAtLeast(1)).ifEmpty { listOf(emptyList()) }
    val pager = rememberPagerState(pageCount = { pages.size })
    HorizontalPager(pager, Modifier.fillMaxSize()) { index ->
        DrawerGrid(pages[index], vm, columns)
    }
}

@Composable
private fun CategoryDrawer(apps: List<LaunchApp>, vm: LauncherViewModel, columns: Int) {
    val groups = remember(apps) {
        apps.groupBy { it.label.firstOrNull()?.uppercaseChar()?.let { c -> if (c in 'A'..'Z') c.toString() else "#" } ?: "#" }
            .toSortedMap()
    }
    val keys = groups.keys.toList()
    val pager = rememberPagerState(pageCount = { keys.size.coerceAtLeast(1) })
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        if (keys.isNotEmpty()) {
            ScrollableTabRow(selectedTabIndex = pager.currentPage.coerceAtMost(keys.lastIndex), containerColor = Color.Transparent, edgePadding = 8.dp) {
                keys.forEachIndexed { index, key ->
                    Tab(
                        selected = pager.currentPage == index,
                        onClick = { scope.launch { pager.animateScrollToPage(index) } },
                        text = { Text(key, color = Color.White, fontFamily = LocalVyxelFont.current) }
                    )
                }
            }
        }
        HorizontalPager(pager, Modifier.weight(1f)) { index ->
            DrawerGrid(groups[keys.getOrNull(index)] ?: emptyList(), vm, columns)
        }
    }
}
