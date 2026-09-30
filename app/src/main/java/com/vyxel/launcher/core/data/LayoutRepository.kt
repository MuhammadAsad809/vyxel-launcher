package com.vyxel.launcher.core.data

import com.vyxel.launcher.core.model.HomeLayout
import com.vyxel.launcher.core.model.HomePage
import com.vyxel.launcher.core.model.ItemType
import com.vyxel.launcher.core.model.PlacedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class LayoutRepository(private val store: JsonStore) {
    private val _layout = MutableStateFlow(HomeLayout())
    val layout: StateFlow<HomeLayout> = _layout.asStateFlow()

    suspend fun load() {
        val raw = store.readText(FILE) ?: return
        runCatching {
            _layout.value = VyxelJson.decodeFromString(HomeLayout.serializer(), raw)
        }
    }

    suspend fun replace(layout: HomeLayout) {
        _layout.value = sanitize(layout)
        persist()
    }

    suspend fun update(transform: (HomeLayout) -> HomeLayout) {
        _layout.value = sanitize(transform(_layout.value))
        persist()
    }

    suspend fun ensureSeeded(dockComponents: List<String>) {
        if (_layout.value.dock.isNotEmpty() || _layout.value.pages.any { it.items.isNotEmpty() }) return
        val dock = dockComponents.take(4).mapIndexed { index, component ->
            PlacedItem(
                id = UUID.randomUUID().toString(),
                type = ItemType.APP,
                cellX = index,
                cellY = 0,
                component = component
            )
        }
        _layout.value = HomeLayout(pages = listOf(HomePage()), dock = dock)
        persist()
    }

    suspend fun addPage(): HomeLayout {
        val next = _layout.value.copy(pages = _layout.value.pages + HomePage())
        _layout.value = next
        persist()
        return next
    }

    suspend fun removeEmptyPages() {
        val pages = _layout.value.pages
        if (pages.size <= 1) return
        val kept = pages.filterIndexed { index, page -> index == 0 || page.items.isNotEmpty() }
        if (kept.size != pages.size) {
            _layout.value = _layout.value.copy(pages = kept.ifEmpty { listOf(HomePage()) })
            persist()
        }
    }

    private fun sanitize(layout: HomeLayout): HomeLayout {
        val pages = layout.pages.ifEmpty { listOf(HomePage()) }
        return layout.copy(pages = pages)
    }

    private suspend fun persist() {
        store.writeText(FILE, VyxelJson.encodeToString(HomeLayout.serializer(), _layout.value))
    }

    companion object {
        const val FILE = "vyxel-layout.json"
    }
}
