package com.vyxel.launcher.core.data

import android.content.Context
import android.net.Uri
import com.vyxel.launcher.core.model.HomeLayout
import com.vyxel.launcher.core.model.LauncherSettings
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class VyxelBackup(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val settings: LauncherSettings,
    val layout: HomeLayout
)

class BackupManager(
    private val context: Context,
    private val settings: SettingsRepository,
    private val layout: LayoutRepository
) {
    suspend fun exportTo(uri: Uri) {
        val backup = VyxelBackup(
            settings = settings.settings.value,
            layout = layout.layout.value
        )
        val json = VyxelJson.encodeToString(VyxelBackup.serializer(), backup)
        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
    }

    suspend fun importFrom(uri: Uri) {
        val json = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            ?: return
        val backup = VyxelJson.decodeFromString(VyxelBackup.serializer(), json)
        settings.replace(backup.settings)
        layout.replace(backup.layout)
    }

    suspend fun snapshotJson(): String {
        val backup = VyxelBackup(
            settings = settings.settings.value,
            layout = layout.layout.value
        )
        return VyxelJson.encodeToString(VyxelBackup.serializer(), backup)
    }

    suspend fun restoreJson(json: String) {
        val backup = VyxelJson.decodeFromString(VyxelBackup.serializer(), json)
        settings.replace(backup.settings)
        layout.replace(backup.layout)
    }

    fun suggestedFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
        return "vyxel-layout-$stamp.json"
    }
}
