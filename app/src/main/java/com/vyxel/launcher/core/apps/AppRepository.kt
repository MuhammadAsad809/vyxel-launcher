package com.vyxel.launcher.core.apps

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Process
import android.os.UserHandle
import com.vyxel.launcher.core.model.LaunchApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {
    private val _apps = MutableStateFlow<List<LaunchApp>>(emptyList())
    val apps: StateFlow<List<LaunchApp>> = _apps.asStateFlow()

    suspend fun reload() {
        _apps.value = withContext(Dispatchers.IO) { query() }
    }

    fun visible(hidden: List<String>): List<LaunchApp> {
        val hide = hidden.toSet()
        return _apps.value.filter { it.component !in hide && it.packageName != context.packageName }
    }

    fun find(component: String): LaunchApp? = _apps.value.find { it.component == component }

    fun launch(app: LaunchApp) {
        runCatching {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setClassName(app.packageName, app.activityName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            context.startActivity(intent)
        }
    }

    fun launchComponent(component: String) {
        find(component)?.let { launch(it) }
    }

    fun appInfo(component: String) {
        val pkg = component.substringBefore("/")
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$pkg")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun uninstall(component: String) {
        val pkg = component.substringBefore("/")
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = android.net.Uri.parse("package:$pkg")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    private fun query(): List<LaunchApp> {
        val pm = context.packageManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
        val user: UserHandle = Process.myUserHandle()
        if (launcherApps != null) {
            val listed = launcherApps.getActivityList(null, user).map { info ->
                LaunchApp(
                    label = info.label?.toString().orEmpty().ifBlank { info.applicationInfo.loadLabel(pm).toString() },
                    packageName = info.applicationInfo.packageName,
                    activityName = info.componentName.className
                )
            }
            return listed.sortedBy { it.label.lowercase() }
        }
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map {
                LaunchApp(
                    label = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    activityName = it.activityInfo.name
                )
            }
            .sortedBy { it.label.lowercase() }
    }
}
