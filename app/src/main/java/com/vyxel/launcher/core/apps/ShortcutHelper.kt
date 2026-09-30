package com.vyxel.launcher.core.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Process
import com.vyxel.launcher.core.model.AppShortcut
import com.vyxel.launcher.core.model.LaunchApp

class ShortcutHelper(private val context: Context) {
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    fun shortcutsFor(app: LaunchApp): List<AppShortcut> {
        if (Build.VERSION.SDK_INT < 25) return emptyList()
        if (!launcherApps.hasShortcutHostPermission()) return emptyList()
        return runCatching {
            val query = LauncherApps.ShortcutQuery().apply {
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                )
                setPackage(app.packageName)
            }
            launcherApps.getShortcuts(query, Process.myUserHandle()).orEmpty().map { s ->
                AppShortcut(
                    id = s.id,
                    label = s.shortLabel?.toString() ?: s.longLabel?.toString().orEmpty(),
                    packageName = app.packageName,
                    intent = s.intent
                )
            }
        }.getOrDefault(emptyList())
    }

    fun startShortcut(app: LaunchApp, shortcutId: String) {
        if (Build.VERSION.SDK_INT < 25) return
        runCatching {
            launcherApps.startShortcut(app.packageName, shortcutId, null, null, Process.myUserHandle())
        }
    }
}
