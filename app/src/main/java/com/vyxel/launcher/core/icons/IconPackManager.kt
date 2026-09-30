package com.vyxel.launcher.core.icons

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import com.vyxel.launcher.core.model.IconPackInfo
import com.vyxel.launcher.core.model.LaunchApp
import org.xmlpull.v1.XmlPullParser

class IconPackManager(private val context: Context) {
    private val cache = LruCache<String, Bitmap>(256)
    private var loadedPack: String? = null
    private val componentToDrawable = mutableMapOf<String, String>()

    fun installedPacks(): List<IconPackInfo> {
        val pm = context.packageManager
        val intents = listOf(
            Intent("org.adw.launcher.THEMES"),
            Intent("com.novalauncher.THEME"),
            Intent("com.gau.go.launcherex.theme"),
            Intent("org.adw.launcher.icons.ACTION_PICK_ICON"),
            Intent("ginlemon.smartlauncher.THEMES")
        )
        val seen = LinkedHashMap<String, IconPackInfo>()
        intents.forEach { intent ->
            pm.queryIntentActivities(intent, PackageManager.GET_META_DATA).forEach { info ->
                val pkg = info.activityInfo.packageName
                if (pkg !in seen) {
                    seen[pkg] = IconPackInfo(pkg, info.loadLabel(pm).toString())
                }
            }
        }
        return seen.values.toList()
    }

    fun loadPack(packageName: String?) {
        if (packageName == loadedPack) return
        componentToDrawable.clear()
        cache.evictAll()
        loadedPack = packageName
        if (packageName.isNullOrBlank()) return
        runCatching {
            val res = context.packageManager.getResourcesForApplication(packageName)
            val id = res.getIdentifier("appfilter", "xml", packageName)
            if (id == 0) return
            val parser = res.getXml(id)
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component")
                    val drawable = parser.getAttributeValue(null, "drawable")
                    if (!component.isNullOrBlank() && !drawable.isNullOrBlank()) {
                        val key = component
                            .removePrefix("ComponentInfo{")
                            .removeSuffix("}")
                        componentToDrawable[key] = drawable
                    }
                }
                event = parser.next()
            }
        }
    }

    fun iconFor(
        app: LaunchApp,
        packPackage: String?,
        custom: Map<String, String>
    ): Drawable {
        val override = custom[app.component]
        val fromPack = drawableFromPack(packPackage, override ?: componentToDrawable[app.component])
        if (fromPack != null) return fromPack
        return runCatching {
            context.packageManager.getActivityIcon(
                android.content.ComponentName(app.packageName, app.activityName)
            )
        }.getOrElse {
            context.packageManager.getApplicationIcon(app.packageName)
        }
    }

    fun bitmapFor(app: LaunchApp, packPackage: String?, custom: Map<String, String>, size: Int): Bitmap {
        val key = "${packPackage.orEmpty()}|${app.component}|${custom[app.component].orEmpty()}|$size"
        cache.get(key)?.let { return it }
        val drawable = iconFor(app, packPackage, custom)
        val bmp = drawable.toBitmap(size, size, Bitmap.Config.ARGB_8888)
        cache.put(key, bmp)
        return bmp
    }

    fun clear() {
        cache.evictAll()
    }

    private fun drawableFromPack(packPackage: String?, drawableName: String?): Drawable? {
        if (packPackage.isNullOrBlank() || drawableName.isNullOrBlank()) return null
        return runCatching {
            val res = context.packageManager.getResourcesForApplication(packPackage)
            val id = res.getIdentifier(drawableName, "drawable", packPackage)
            if (id == 0) null else res.getDrawable(id, null)
        }.getOrNull()
    }
}

fun Drawable.toSafeBitmap(size: Int): Bitmap {
    if (this is BitmapDrawable && bitmap != null) {
        return Bitmap.createScaledBitmap(bitmap, size, size, true)
    }
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, size, size)
    draw(canvas)
    return bmp
}
