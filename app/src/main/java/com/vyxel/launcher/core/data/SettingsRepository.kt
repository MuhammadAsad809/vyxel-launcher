package com.vyxel.launcher.core.data

import com.vyxel.launcher.core.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepository(private val store: JsonStore) {
    private val _settings = MutableStateFlow(LauncherSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    suspend fun load() {
        val raw = store.readText(FILE) ?: return
        runCatching {
            _settings.value = VyxelJson.decodeFromString(LauncherSettings.serializer(), raw)
        }
    }

    suspend fun update(transform: (LauncherSettings) -> LauncherSettings) {
        _settings.update(transform)
        persist()
    }

    suspend fun replace(settings: LauncherSettings) {
        _settings.value = settings
        persist()
    }

    private suspend fun persist() {
        store.writeText(FILE, VyxelJson.encodeToString(LauncherSettings.serializer(), _settings.value))
    }

    companion object {
        const val FILE = "vyxel-settings.json"
    }
}
