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

        @Volatile
        private var activeService: LauncherNotificationService? = null

        fun isPermissionGranted(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val myComponent = ComponentName(context, LauncherNotificationService::class.java).flattenToString()
            return enabledListeners.contains(myComponent)
        }

        fun dismissNotificationsForPackage(packageName: String): Boolean {
            return activeService?.cancelPackageNotifications(packageName) ?: false
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        activeService = this
        _isServiceConnected.value = true
        refreshAllNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (activeService === this) {
            activeService = null
        }
        _isServiceConnected.value = false
        _notifications.value = emptyMap()
    }

    override fun onDestroy() {
        if (activeService === this) {
            activeService = null
        }
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        refreshAllNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        refreshAllNotifications()
    }

    private fun cancelPackageNotifications(packageName: String): Boolean {
        val keysToCancel = linkedSetOf<String>()
        try {
            val active = activeNotifications
            if (active != null) {
                for (sbn in active) {
                    if (
                        sbn.packageName == packageName &&
                        sbn.isClearable &&
                        (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT) == 0
                    ) {
                        keysToCancel.add(sbn.key)
                    }
                }
            }
        } catch (_: Exception) {
            // Fall back to tracked clearable notification keys below
        }

        _notifications.value[packageName]
            .orEmpty()
            .filter { it.isClearable }
            .forEach { keysToCancel.add(it.key) }

        if (keysToCancel.isEmpty()) return false

        try {
            cancelNotifications(keysToCancel.toTypedArray())
        } catch (_: Exception) {
            for (key in keysToCancel) {
                try {
                    cancelNotification(key)
                } catch (_: Exception) {
                }
            }
        }

        _notifications.value = NotificationStateUtils.removeDismissedKeys(
            current = _notifications.value,
            packageName = packageName,
            dismissedKeys = keysToCancel
        )
        return true
    }

    private fun refreshAllNotifications() {
        try {
            val active = activeNotifications ?: return
            val map = mutableMapOf<String, MutableList<RankedNotification>>()

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
                    postTime = sbn.postTime,
                    isClearable = sbn.isClearable
                )
                val isGroupSummary = (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0

                map.getOrPut(pkg) { mutableListOf() }.add(RankedNotification(item, isGroupSummary))
            }
            _notifications.value = map.mapValues { (_, items) ->
                NotificationStateUtils.sortPackageNotifications(items)
            }
        } catch (_: Exception) {
            // Service might not be ready yet
        }
    }
}
