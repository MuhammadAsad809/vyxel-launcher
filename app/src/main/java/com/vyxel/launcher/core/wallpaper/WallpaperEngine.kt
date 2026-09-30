package com.vyxel.launcher.core.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WallpaperEngine(private val context: Context) {
    private val customFile = File(context.filesDir, "vyxel-wallpaper.jpg")

    suspend fun currentBitmap(): Bitmap? = withContext(Dispatchers.IO) {
        if (customFile.exists()) {
            return@withContext BitmapFactory.decodeFile(customFile.absolutePath)
        }
        runCatching {
            val drawable = WallpaperManager.getInstance(context).drawable
            (drawable as? BitmapDrawable)?.bitmap
        }.getOrNull()
    }

    suspend fun importFrom(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                customFile.outputStream().use { output -> input.copyTo(output) }
            }
            true
        }.getOrDefault(false)
    }

    suspend fun applySystemWallpaper(): Boolean = withContext(Dispatchers.IO) {
        val bmp = currentBitmap() ?: return@withContext false
        runCatching {
            WallpaperManager.getInstance(context).setBitmap(bmp)
            true
        }.getOrDefault(false)
    }

    fun clearCustom() {
        customFile.delete()
    }

    fun hasCustom(): Boolean = customFile.exists()
}
