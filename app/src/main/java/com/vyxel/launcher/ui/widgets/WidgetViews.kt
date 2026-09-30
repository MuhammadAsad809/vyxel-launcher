package com.vyxel.launcher.ui.widgets

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.viewinterop.AndroidView
import com.vyxel.launcher.core.widgets.WidgetHostManager
import com.vyxel.launcher.ui.LauncherViewModel
import com.vyxel.launcher.ui.theme.GlassSurface
import com.vyxel.launcher.ui.theme.LocalVyxelFont
import kotlin.math.max

@Composable
fun BoundWidget(id: Int, host: WidgetHostManager, modifier: Modifier = Modifier) {
    val view = remember(id) { host.createView(id) }
    if (view == null) {
        Text("Widget unavailable", color = Color.White.copy(alpha = 0.6f), modifier = modifier.padding(8.dp))
        return
    }
    AndroidView(
        factory = { view },
        update = { v ->
            val info = host.infoFor(id)
            if (info != null && v is AppWidgetHostView) v.setAppWidget(id, info)
        },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun WidgetPicker(vm: LauncherViewModel, onClose: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val providers = remember { vm.widgetProviders() }
    val host = vm.widgets()
    val bind = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val id = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        if (id != -1) {
            val info = host.infoFor(id)
            if (info != null) {
                val spanX = max(1, info.minWidth / 80).coerceAtMost(settings.gridColumns)
                val spanY = max(1, info.minHeight / 90).coerceAtMost(3)
                vm.attachWidget(id, info.provider.flattenToShortString(), spanX, spanY)
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        GlassSurface(settings = settings, radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Widgets", color = Color.White, fontSize = 20.sp, fontFamily = LocalVyxelFont.current, modifier = Modifier.weight(1f))
                Text("Clock", color = Color(0xFF6BA3F5), modifier = Modifier.clickable { vm.addClockWidget(); onClose() }.padding(8.dp))
                Text("Weather", color = Color(0xFF6BA3F5), modifier = Modifier.clickable { vm.addWeatherWidget(); onClose() }.padding(8.dp))
                Text("Close", color = Color(0xFF6BA3F5), modifier = Modifier.clickable(onClick = onClose).padding(8.dp))
            }
        }
        LazyColumn(Modifier.padding(top = 12.dp)) {
            items(providers, key = { it.packageName + it.className }) { p ->
                GlassSurface(settings = settings, radius = 16.dp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val id = host.allocate()
                                val cn = ComponentName(p.packageName, p.className)
                                if (host.bindIfAllowed(id, cn)) {
                                    val spanX = max(1, p.minWidth / 80).coerceAtMost(settings.gridColumns)
                                    val spanY = max(1, p.minHeight / 90).coerceAtMost(3)
                                    vm.attachWidget(id, cn.flattenToShortString(), spanX, spanY)
                                } else {
                                    bind.launch(host.bindIntent(id, cn))
                                }
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        p.preview?.let { d ->
                            AndroidView(
                                factory = { ctx ->
                                    android.widget.ImageView(ctx).apply {
                                        setImageDrawable(d)
                                        adjustViewBounds = true
                                    }
                                },
                                modifier = Modifier.size(48.dp).padding(end = 10.dp)
                            )
                        }
                        Column {
                            Text(p.label, color = Color.White, fontFamily = LocalVyxelFont.current)
                            Text(p.packageName, color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
