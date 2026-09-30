package com.vyxel.launcher.core.search

import com.vyxel.launcher.core.model.LaunchApp
import com.vyxel.launcher.core.model.SearchHit

class SearchEngine {
    fun query(apps: List<LaunchApp>, raw: String, hidden: Set<String>): List<SearchHit> {
        val q = raw.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return apps.asSequence()
            .filter { it.component !in hidden }
            .map { app -> SearchHit(app, score(app, q)) }
            .filter { it.score > 0 }
            .sortedWith(compareByDescending<SearchHit> { it.score }.thenBy { it.app.label.lowercase() })
            .take(40)
            .toList()
    }

    private fun score(app: LaunchApp, q: String): Int {
        val label = app.label.lowercase()
        val pkg = app.packageName.lowercase()
        return when {
            label == q -> 100
            label.startsWith(q) -> 80
            acronym(label).startsWith(q) -> 70
            label.contains(q) -> 50
            pkg.contains(q) -> 20
            fuzzy(label, q) -> 12
            else -> 0
        }
    }

    private fun acronym(label: String): String =
        label.split(' ', '.', '-', '_').filter { it.isNotEmpty() }.joinToString("") { it.first().toString() }

    private fun fuzzy(label: String, q: String): Boolean {
        var i = 0
        for (c in label) {
            if (i < q.length && c == q[i]) i++
        }
        return i == q.length
    }
}
