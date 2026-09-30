package com.vyxel.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import com.vyxel.launcher.core.gestures.GestureController
import com.vyxel.launcher.core.model.GestureTrigger
import com.vyxel.launcher.core.theme.ThemeEngine
import com.vyxel.launcher.ui.drawer.AppDrawer
import com.vyxel.launcher.ui.home.FolderSheet
import com.vyxel.launcher.ui.home.HomeQuickActions
import com.vyxel.launcher.ui.home.HomeScreen
import com.vyxel.launcher.ui.menu.AppContextMenu
import com.vyxel.launcher.ui.notifications.NotificationShade
import com.vyxel.launcher.ui.search.Spotlight
import com.vyxel.launcher.ui.settings.SettingsScreen
import com.vyxel.launcher.ui.theme.VyxelTheme
import com.vyxel.launcher.ui.widgets.WidgetPicker
import kotlin.math.roundToInt

@Composable
fun LauncherRoot(vm: LauncherViewModel, systemDark: Boolean, themeEngine: ThemeEngine) {
    val settings by vm.settings.collectAsState()
    val overlay by vm.overlay.collectAsState()
    val wallpaper by vm.wallpaper.collectAsState()
    val folder by vm.folder.collectAsState()
    val menuApp by vm.menuApp.collectAsState()
    val edit by vm.editMode.collectAsState()
    val snapshot = remember(settings, systemDark, wallpaper) {
        themeEngine.resolve(settings, systemDark, wallpaper)
    }
    val context = LocalContext.current
    var parallax by remember { mutableFloatStateOf(0f) }

    BackHandler(enabled = overlay != Overlay.NONE || edit) {
        if (edit) vm.toggleEdit() else vm.closeOverlays()
    }

    VyxelTheme(snapshot, settings.fontFamily) {
        Box(
            Modifier
                .fillMaxSize()
                .background(snapshot.background)
                .pointerInput(settings.gestures) {
                    var acc = 0f
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (acc > GestureController.FAST_SWIPE) {
                                vm.handleTrigger(GestureTrigger.SWIPE_DOWN_STATUS, context)
                            }
                            acc = 0f
                        }
                    ) { change, drag ->
                        acc += drag
                        if (settings.wallpaperParallax) {
                            parallax = (parallax + change.position.x / 80f).coerceIn(-24f, 24f)
                        }
                    }
                }
        ) {
            if (wallpaper != null) {
                Image(
                    bitmap = wallpaper!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(if (settings.wallpaperParallax) parallax.roundToInt() else 0, 0) }
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0B0E14), Color(0xFF151A24), Color(0xFF1C2333))
                            )
                        )
                )
            }
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = settings.wallpaperDim)))

            HomeScreen(vm)

            if (edit) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    HomeQuickActions(vm)
                }
            }

            AnimatedContent(
                targetState = overlay,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "overlay"
            ) { current ->
                when (current) {
                    Overlay.NONE -> Box(Modifier)
                    Overlay.DRAWER -> AppDrawer(vm) { vm.closeOverlays() }
                    Overlay.SEARCH -> Spotlight(vm) { vm.closeOverlays() }
                    Overlay.NOTIFICATIONS -> NotificationShade(vm) { vm.closeOverlays() }
                    Overlay.SETTINGS -> SettingsScreen(vm) { vm.closeOverlays() }
                    Overlay.WIDGETS -> WidgetPicker(vm) { vm.closeOverlays() }
                    Overlay.FOLDER -> folder?.let { FolderSheet(it, settings, vm) { vm.closeOverlays() } }
                    Overlay.MENU -> Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable { vm.closeOverlays() },
                        contentAlignment = Alignment.Center
                    ) {
                        menuApp?.let { AppContextMenu(it, vm) { vm.closeOverlays() } }
                    }
                }
            }
        }
    }
}
