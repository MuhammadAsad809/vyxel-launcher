package com.vyxel.launcher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vyxel.launcher.VyxelApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackageChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as? VyxelApplication ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.container.apps.reload()
                app.container.icons.clear()
            } finally {
                pending.finish()
            }
        }
    }
}
