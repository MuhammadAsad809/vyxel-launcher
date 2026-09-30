package com.vyxel.launcher.core.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

val VyxelJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = true
}

class JsonStore(private val context: Context) {
    private val mutex = Mutex()

    suspend fun readText(name: String): String? = withContext(Dispatchers.IO) {
        mutex.withLock {
            val file = File(context.filesDir, name)
            if (file.exists()) file.readText() else null
        }
    }

    suspend fun writeText(name: String, text: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val file = File(context.filesDir, name)
            val tmp = File(context.filesDir, "$name.tmp")
            tmp.writeText(text)
            if (!tmp.renameTo(file)) {
                file.writeText(text)
                tmp.delete()
            }
        }
    }

    suspend fun delete(name: String) = withContext(Dispatchers.IO) {
        mutex.withLock { File(context.filesDir, name).delete() }
    }
}
