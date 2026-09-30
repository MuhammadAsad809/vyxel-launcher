package com.vyxel.launcher.core.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.vyxel.launcher.core.model.WidgetProviderInfo as VyxelProvider

class VyxelAppWidgetHost(context: Context, hostId: Int) : AppWidgetHost(context, hostId)

class WidgetHostManager(private val context: Context) {
    val host = VyxelAppWidgetHost(context, HOST_ID)
    val manager: AppWidgetManager = AppWidgetManager.getInstance(context)

    fun start() = host.startListening()
    fun stop() = runCatching { host.stopListening() }

    fun providers(): List<VyxelProvider> {
        return manager.installedProviders.map { info ->
            VyxelProvider(
                packageName = info.provider.packageName,
                className = info.provider.className,
                label = info.loadLabel(context.packageManager),
                minWidth = info.minWidth,
                minHeight = info.minHeight,
                preview = runCatching { info.loadPreviewImage(context, 0) }.getOrNull()
                    ?: runCatching { info.loadIcon(context, 0) }.getOrNull()
            )
        }.sortedBy { it.label.lowercase() }
    }

    fun allocate(): Int = host.allocateAppWidgetId()

    fun delete(id: Int) {
        runCatching { host.deleteAppWidgetId(id) }
    }

    fun bindIfAllowed(id: Int, provider: ComponentName): Boolean {
        return manager.bindAppWidgetIdIfAllowed(id, provider)
    }

    fun bindIntent(id: Int, provider: ComponentName): Intent {
        return Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }
    }

    fun configureIntent(id: Int, info: AppWidgetProviderInfo): Intent? {
        if (info.configure == null) return null
        return Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = info.configure
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        }
    }

    fun infoFor(id: Int): AppWidgetProviderInfo? = manager.getAppWidgetInfo(id)

    fun createView(id: Int): AppWidgetHostView? {
        val info = infoFor(id) ?: return null
        return host.createView(context, id, info)
    }

    fun providerInfo(packageName: String, className: String): AppWidgetProviderInfo? {
        val cn = ComponentName(packageName, className)
        return manager.installedProviders.find { it.provider == cn }
    }

    companion object {
        const val HOST_ID = 0x564C58 // VLX
    }
}
