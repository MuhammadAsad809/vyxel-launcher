package com.vyxel.launcher

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vyxel.launcher.ui.LauncherRoot
import com.vyxel.launcher.ui.LauncherViewModel
import com.vyxel.launcher.ui.Overlay

class MainActivity : ComponentActivity() {
    private val vm: LauncherViewModel by viewModels { LauncherViewModel.Factory() }
    private val widgets get() = (application as VyxelApplication).container.widgets

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        maybeRequestLocation()
        setContent {
            val dark = isSystemInDarkTheme() ||
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> {
                            widgets.start()
                            vm.reloadApps()
                        }
                        Lifecycle.Event.ON_STOP -> widgets.stop()
                        else -> Unit
                    }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }
            LauncherRoot(
                vm = vm,
                systemDark = dark,
                themeEngine = (application as VyxelApplication).container.theme
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        vm.closeOverlays()
        vm.setPage(0)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (vm.overlay.value != Overlay.NONE) {
            vm.closeOverlays()
            return
        }
        if (vm.editMode.value) {
            vm.toggleEdit()
            return
        }
        vm.setPage(0)
    }

    private fun maybeRequestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 42)
        }
    }
}
