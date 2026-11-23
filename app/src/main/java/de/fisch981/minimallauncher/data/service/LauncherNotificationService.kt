package de.fisch981.minimallauncher.data.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import de.fisch981.minimallauncher.data.model.AppNotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherNotificationService : NotificationListenerService() {

    companion object {
        private val _notifications = MutableStateFlow<Map<String, List<AppNotificationItem>>>(emptyMap())
        val notifications: StateFlow<Map<String, List<AppNotificationItem>>> = _notifications.asStateFlow()

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        fun isPermissionGranted(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val myComponent = ComponentName(context, LauncherNotificationService::class.java).flattenToString()
            return enabledListeners.contains(myComponent)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isServiceConnected.value = true
        refreshAllNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isServiceConnected.value = false
        _notifications.value = emptyMap()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        refreshAllNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        refreshAllNotifications()
    }

    private fun refreshAllNotifications() {
        try {
            val active = activeNotifications ?: return
            val map = mutableMapOf<String, MutableList<AppNotificationItem>>()

            for (sbn in active) {
                // Ignore ongoing system notifications (like charging, persistent foreground service)
                if ((sbn.notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) continue

                val pkg = sbn.packageName ?: continue
                val extras = sbn.notification.extras
                val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                    ?: extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

                if (title.isNullOrBlank() && text.isNullOrBlank()) continue

                val item = AppNotificationItem(
                    key = sbn.key,
                    packageName = pkg,
                    title = title,
                    text = text,
                    postTime = sbn.postTime
                )

                map.getOrPut(pkg) { mutableListOf() }.add(item)
            }
            _notifications.value = map
        } catch (_: Exception) {
            // Service might not be ready yet
        }
    }
}
