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

        private val locallyDismissedLock = Any()
        private var locallyDismissedKeyPostTimes: Map<String, Long> = emptyMap()

        fun isPermissionGranted(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val myComponent = ComponentName(context, LauncherNotificationService::class.java).flattenToString()
            return enabledListeners.contains(myComponent)
        }

        fun syncOrRebind(context: Context) {
            if (!isPermissionGranted(context)) {
                synchronized(locallyDismissedLock) {
                    locallyDismissedKeyPostTimes = emptyMap()
                }
                _notifications.value = emptyMap()
                return
            }

            val service = activeService
            if (service != null) {
                service.refreshAllNotifications()
            } else {
                try {
                    requestRebind(ComponentName(context, LauncherNotificationService::class.java))
                } catch (_: Exception) {
                    // System may restrict rebind if process or listener state is transitioning
                }
            }
        }

        fun dismissNotificationsForPackage(packageName: String): Boolean {
            val service = activeService
            if (service != null) {
                return service.cancelPackageNotifications(packageName)
            }

            val currentItems = _notifications.value[packageName].orEmpty()
            if (currentItems.isEmpty()) return false

            val dismissedUpdates = currentItems.associate { it.key to it.postTime }
            synchronized(locallyDismissedLock) {
                val merged = locallyDismissedKeyPostTimes.toMutableMap()
                for ((key, postTime) in dismissedUpdates) {
                    val existing = merged[key] ?: Long.MIN_VALUE
                    if (postTime > existing) merged[key] = postTime
                }
                locallyDismissedKeyPostTimes = merged
            }

            _notifications.value = NotificationStateUtils.removeDismissedKeys(
                current = _notifications.value,
                packageName = packageName,
                dismissedKeys = dismissedUpdates.keys
            )
            return true
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
        val dismissedUpdates = linkedMapOf<String, Long>()
        try {
            val active = activeNotifications
            if (active != null) {
                for (sbn in active) {
                    if (sbn.packageName == packageName) {
                        val isOngoing = sbn.isOngoing || (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
                        if (!isOngoing) {
                            dismissedUpdates[sbn.key] = sbn.postTime
                            if (sbn.isClearable) {
                                keysToCancel.add(sbn.key)
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Fall back to tracked notification keys below
        }

        _notifications.value[packageName]
            .orEmpty()
            .forEach { item ->
                val prev = dismissedUpdates[item.key] ?: Long.MIN_VALUE
                if (item.postTime > prev) {
                    dismissedUpdates[item.key] = item.postTime
                }
                if (item.isClearable) {
                    keysToCancel.add(item.key)
                }
            }

        if (dismissedUpdates.isEmpty() && keysToCancel.isEmpty()) return false

        if (dismissedUpdates.isNotEmpty()) {
            synchronized(locallyDismissedLock) {
                val merged = locallyDismissedKeyPostTimes.toMutableMap()
                for ((key, postTime) in dismissedUpdates) {
                    val existing = merged[key] ?: Long.MIN_VALUE
                    if (postTime > existing) merged[key] = postTime
                }
                locallyDismissedKeyPostTimes = merged
            }
        }

        if (keysToCancel.isNotEmpty()) {
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
        }

        _notifications.value = NotificationStateUtils.removeDismissedKeys(
            current = _notifications.value,
            packageName = packageName,
            dismissedKeys = dismissedUpdates.keys
        )
        return true
    }

    private fun refreshAllNotifications() {
        try {
            val active = activeNotifications ?: return
            val map = mutableMapOf<String, MutableList<RankedNotification>>()
            val liveActiveKeyPostTimes = mutableMapOf<String, Long>()

            for (sbn in active) {
                val flags = sbn.notification.flags
                val isOngoing = sbn.isOngoing || (flags and Notification.FLAG_ONGOING_EVENT) != 0
                val isNoClear = (flags and Notification.FLAG_NO_CLEAR) != 0
                val extras = sbn.notification.extras
                val hasMediaSession = extras?.containsKey(Notification.EXTRA_MEDIA_SESSION) == true
                val template = extras?.getString(Notification.EXTRA_TEMPLATE)

                if (
                    !NotificationStateUtils.shouldTrackNotification(
                        isOngoing = isOngoing,
                        isClearable = sbn.isClearable,
                        isNoClear = isNoClear,
                        hasMediaSession = hasMediaSession,
                        category = sbn.notification.category,
                        template = template
                    )
                ) {
                    continue
                }

                liveActiveKeyPostTimes[sbn.key] = sbn.postTime

                val pkg = sbn.packageName ?: continue
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
                val isGroupSummary = (flags and Notification.FLAG_GROUP_SUMMARY) != 0

                map.getOrPut(pkg) { mutableListOf() }.add(RankedNotification(item, isGroupSummary))
            }

            val dismissedSnapshot = synchronized(locallyDismissedLock) {
                NotificationStateUtils.pruneDismissedKeys(
                    dismissedKeyPostTimes = locallyDismissedKeyPostTimes,
                    liveActiveKeysWithPostTime = liveActiveKeyPostTimes
                ).also { pruned ->
                    locallyDismissedKeyPostTimes = pruned
                }
            }

            val filtered = mutableMapOf<String, List<AppNotificationItem>>()
            for ((pkg, items) in map) {
                val visible = NotificationStateUtils.filterLocallyDismissed(items, dismissedSnapshot)
                if (visible.isNotEmpty()) {
                    filtered[pkg] = NotificationStateUtils.sortPackageNotifications(visible)
                }
            }
            _notifications.value = filtered
        } catch (_: Exception) {
            // Service might not be ready yet
        }
    }
}
