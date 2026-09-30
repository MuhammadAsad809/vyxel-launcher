package com.vyxel.launcher.ui.notifications

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyxel.launcher.core.notifications.NotificationMirrorService
import com.vyxel.launcher.ui.LauncherViewModel
import com.vyxel.launcher.ui.theme.GlassSurface
import com.vyxel.launcher.ui.theme.LocalVyxelFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationShade(vm: LauncherViewModel, onClose: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val notes by vm.notifications.collectAsState()
    val context = LocalContext.current
    val connected = NotificationMirrorService.connected()

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onClose)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        GlassSurface(settings = settings, radius = 24.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Notifications", color = Color.White, fontSize = 22.sp, fontFamily = LocalVyxelFont.current)
                    Text("Close", color = Color(0xFF6BA3F5), modifier = Modifier.clickable(onClick = onClose))
                }
                if (!connected) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Grant notification access to mirror the system shade.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Open settings",
                        color = Color(0xFF0A84FF),
                        modifier = Modifier.clickable { openListenerSettings(context) }
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Clear all",
                        color = Color(0xFF0A84FF),
                        modifier = Modifier.clickable { NotificationMirrorService.cancelAll() }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notes, key = { it.key }) { n ->
                GlassSurface(settings = settings, radius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { NotificationMirrorService.cancel(n.key) }
                            .padding(14.dp)
                    ) {
                        Text(n.title.ifBlank { n.packageName }, color = Color.White, fontSize = 15.sp, fontFamily = LocalVyxelFont.current)
                        if (n.text.isNotBlank()) {
                            Text(n.text, color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
                        }
                        Text(
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(n.time)),
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

private fun openListenerSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
