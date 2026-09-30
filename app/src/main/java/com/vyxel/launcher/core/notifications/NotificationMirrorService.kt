package com.vyxel.launcher.core.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.vyxel.launcher.core.model.ShadeNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationMirrorService : NotificationListenerService() {
    override fun onListenerConnected() {
        instance = this
        pushAll()
    }

    override fun onListenerDisconnected() {
        if (instance === this) instance = null
        _notifications.value = emptyList()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        pushAll()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        pushAll()
    }

    private fun pushAll() {
        _notifications.value = runCatching {
            activeNotifications.orEmpty()
                .filter { !it.isOngoing }
                .map { it.toShade() }
                .sortedByDescending { it.time }
        }.getOrDefault(emptyList())
    }

    private fun StatusBarNotification.toShade(): ShadeNotification {
        val extras = notification.extras
        return ShadeNotification(
            key = key,
            packageName = packageName,
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty(),
            time = postTime,
            category = notification.category
        )
    }

    companion object {
        private val _notifications = MutableStateFlow<List<ShadeNotification>>(emptyList())
        val notifications: StateFlow<List<ShadeNotification>> = _notifications.asStateFlow()
        @Volatile var instance: NotificationMirrorService? = null
            private set

        fun cancel(key: String) {
            instance?.cancelNotification(key)
        }

        fun cancelAll() {
            instance?.cancelAllNotifications()
        }

        fun connected(): Boolean = instance != null
    }
}
