package com.vyxel.launcher.ui.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vyxel.launcher.core.gestures.GestureController
import com.vyxel.launcher.core.model.GestureTrigger
import com.vyxel.launcher.ui.LauncherViewModel
import com.vyxel.launcher.ui.Overlay
import com.vyxel.launcher.ui.components.ClockFace
import com.vyxel.launcher.ui.components.PageDots
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(vm: LauncherViewModel) {
    val settings by vm.settings.collectAsState()
    val layout by vm.layout.collectAsState()
    val page by vm.page.collectAsState()
    val weather by vm.weather.collectAsState()
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = page, pageCount = { layout.pages.size.coerceAtLeast(1) })
    val scope = rememberCoroutineScope()

    LaunchedEffect(page, layout.pages.size) {
        val target = page.coerceIn(0, (layout.pages.size - 1).coerceAtLeast(0))
        if (pagerState.currentPage != target) pagerState.animateScrollToPage(target)
    }
    LaunchedEffect(pagerState.currentPage) { vm.setPage(pagerState.currentPage) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(settings.gestures) {
                detectTapGestures(
                    onDoubleTap = { vm.handleTrigger(GestureTrigger.DOUBLE_TAP, context) },
                    onLongPress = { vm.toggleEdit() }
                )
            }
            .pointerInput(settings.gestures) {
                var acc = 0f
                detectVerticalDragGestures(
                    onDragEnd = {
                        when {
                            acc < -GestureController.SWIPE_THRESHOLD ->
                                vm.handleTrigger(GestureTrigger.SWIPE_UP, context)
                            acc > GestureController.SWIPE_THRESHOLD ->
                                vm.handleTrigger(GestureTrigger.SWIPE_DOWN, context)
                        }
                        acc = 0f
                    }
                ) { _, drag -> acc += drag }
            }
            .pointerInput(settings.gestures) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom < 0.92f) vm.handleTrigger(GestureTrigger.PINCH_IN, context)
                }
            }
    ) {
        if (settings.showClock) {
            ClockFace(
                style = settings.clockStyle,
                weather = weather,
                showWeather = settings.showWeather,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            beyondViewportPageCount = 1
        ) { index ->
            val homePage = layout.pages.getOrNull(index)
            if (homePage != null) {
                HomePageGrid(page = homePage, settings = settings, vm = vm, modifier = Modifier.fillMaxSize())
            }
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PageDots(
                count = layout.pages.size,
                selected = pagerState.currentPage,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        DockBar(
            items = layout.dock,
            settings = settings,
            vm = vm,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
        )
        Spacer(Modifier.height(4.dp))
    }

    LaunchedEffect(layout.pages.size) {
        if (layout.pages.isNotEmpty() && pagerState.currentPage > layout.pages.lastIndex) {
            scope.launch { pagerState.scrollToPage(layout.pages.lastIndex) }
        }
    }
}

@Composable
fun HomeQuickActions(vm: LauncherViewModel) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = { vm.setOverlay(Overlay.WIDGETS) }) { Text("Widgets", color = Color.White) }
        TextButton(onClick = { vm.addPage() }) { Text("Add page", color = Color.White) }
        TextButton(onClick = { vm.setOverlay(Overlay.SETTINGS) }) { Text("Settings", color = Color.White) }
    }
}
