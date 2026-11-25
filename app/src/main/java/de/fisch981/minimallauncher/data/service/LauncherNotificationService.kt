package de.fisch981.minimallauncher.data.service

import android.app.ActivityOptions
import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
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

        @Volatile
        private var isLauncherVisible: Boolean = false

        @Volatile
        private var hasPendingRefresh: Boolean = false

        private const val NOTIFICATION_DEBOUNCE_MS = 120L

        private val locallyDismissedLock = Any()
        private var locallyDismissedKeyPostTimes: Map<String, Long> = emptyMap()

        fun setLauncherVisible(visible: Boolean, context: Context) {
            isLauncherVisible = visible
            if (!visible) {
                activeService?.cancelPendingDebouncedRefresh()
            } else if (hasPendingRefresh) {
                hasPendingRefresh = false
                syncOrRebind(context)
            }
        }

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

        fun openNotificationForPackage(packageName: String): Boolean {
            return activeService?.launchPackageNotificationIntent(packageName) ?: false
        }

        fun triggerNotificationAction(notificationKey: String, actionIndex: Int): Boolean {
            return activeService?.executeNotificationAction(notificationKey, actionIndex) ?: false
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val debouncedRefreshRunnable = Runnable { refreshAllNotifications() }

    private fun cancelPendingDebouncedRefresh() {
        mainHandler.removeCallbacks(debouncedRefreshRunnable)
    }

    private fun scheduleNotificationRefresh() {
        if (!isLauncherVisible) {
            hasPendingRefresh = true
            return
        }
        mainHandler.removeCallbacks(debouncedRefreshRunnable)
        mainHandler.postDelayed(debouncedRefreshRunnable, NOTIFICATION_DEBOUNCE_MS)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        activeService = this
        _isServiceConnected.value = true
        hasPendingRefresh = false
        refreshAllNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        cancelPendingDebouncedRefresh()
        if (activeService === this) {
            activeService = null
        }
        _isServiceConnected.value = false
        _notifications.value = emptyMap()
    }

    override fun onDestroy() {
        cancelPendingDebouncedRefresh()
        if (activeService === this) {
            activeService = null
        }
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        scheduleNotificationRefresh()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        scheduleNotificationRefresh()
    }

    private fun isLowImportance(
        rankingMap: RankingMap?,
        ranking: Ranking,
        key: String
    ): Boolean {
        if (rankingMap == null || !rankingMap.getRanking(key, ranking)) return false
        return ranking.importance <= NotificationManager.IMPORTANCE_MIN
    }

    private fun isTrackableSbn(
        sbn: StatusBarNotification,
        rankingMap: RankingMap?,
        ranking: Ranking,
        allowDismissibleOngoing: Boolean
    ): Boolean {
        val flags = sbn.notification.flags
        val extras = sbn.notification.extras
        return NotificationStateUtils.shouldTrackNotification(
            packageName = sbn.packageName ?: "",
            isOngoing = sbn.isOngoing || (flags and Notification.FLAG_ONGOING_EVENT) != 0,
            isForegroundService = (flags and Notification.FLAG_FOREGROUND_SERVICE) != 0,
            isClearable = sbn.isClearable,
            isNoClear = (flags and Notification.FLAG_NO_CLEAR) != 0,
            hasMediaSession = extras?.containsKey(Notification.EXTRA_MEDIA_SESSION) == true,
            category = sbn.notification.category,
            template = extras?.getString(Notification.EXTRA_TEMPLATE),
            isLowImportance = isLowImportance(rankingMap, ranking, sbn.key),
            allowDismissibleOngoing = allowDismissibleOngoing
        )
    }

    private fun launchPackageNotificationIntent(packageName: String): Boolean {
        val trackedKeys = _notifications.value[packageName].orEmpty().map { it.key }.toSet()
        val intent = try {
            activeNotifications
                ?.filter { it.packageName == packageName && it.notification.contentIntent != null }
                ?.sortedWith(
                    compareByDescending<StatusBarNotification> { it.key in trackedKeys }
                        .thenByDescending { it.postTime }
                )
                ?.firstOrNull()
                ?.notification
                ?.contentIntent
        } catch (_: Exception) {
            null
        } ?: return false

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic()
                    .setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                    .toBundle()
                intent.send(this, 0, null, null, null, null, options)
            } else {
                intent.send()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun executeNotificationAction(notificationKey: String, actionIndex: Int): Boolean {
        val actionIntent = try {
            activeNotifications
                ?.firstOrNull { it.key == notificationKey }
                ?.notification
                ?.actions
                ?.getOrNull(actionIndex)
                ?.actionIntent
        } catch (_: Exception) {
            null
        } ?: return false

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic()
                    .setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                    .toBundle()
                actionIntent.send(this, 0, null, null, null, null, options)
            } else {
                actionIntent.send()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun cancelPackageNotifications(packageName: String): Boolean {
        val keysToCancel = linkedSetOf<String>()
        val dismissedUpdates = linkedMapOf<String, Long>()
        val rankingMap = try { currentRanking } catch (_: Exception) { null }
        val ranking = Ranking()
        val allowDismissibleOngoing = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

        try {
            val active = activeNotifications
            if (active != null) {
                for (sbn in active) {
                    if (sbn.packageName == packageName) {
                        val flags = sbn.notification.flags
                        val isOngoing = sbn.isOngoing || (flags and Notification.FLAG_ONGOING_EVENT) != 0
                        val isForegroundService = (flags and Notification.FLAG_FOREGROUND_SERVICE) != 0
                        val extras = sbn.notification.extras
                        val isMedia = NotificationStateUtils.isMediaNotification(
                            hasMediaSession = extras?.containsKey(Notification.EXTRA_MEDIA_SESSION) == true,
                            category = sbn.notification.category,
                            template = extras?.getString(Notification.EXTRA_TEMPLATE)
                        )
                        val isTracked = isTrackableSbn(sbn, rankingMap, ranking, allowDismissibleOngoing)
                        val isClearableNonMedia = sbn.isClearable && !isOngoing && !isForegroundService && !isMedia

                        if (isTracked || isClearableNonMedia) {
                            dismissedUpdates[sbn.key] = sbn.postTime
                            keysToCancel.add(sbn.key)
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
                keysToCancel.add(item.key)
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
        hasPendingRefresh = false
        try {
            val active = activeNotifications ?: return
            val map = mutableMapOf<String, MutableList<RankedNotification>>()
            val liveActiveKeyPostTimes = mutableMapOf<String, Long>()
            val rankingMap = try { currentRanking } catch (_: Exception) { null }
            val ranking = Ranking()
            val allowDismissibleOngoing = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

            for (sbn in active) {
                if (!isTrackableSbn(sbn, rankingMap, ranking, allowDismissibleOngoing)) {
                    continue
                }

                liveActiveKeyPostTimes[sbn.key] = sbn.postTime

                val pkg = sbn.packageName ?: continue
                val extras = sbn.notification.extras
                val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                    ?: extras?.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
                val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                    ?: extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                    ?: extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
                    ?: extras?.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()

                if (title.isNullOrBlank() && text.isNullOrBlank()) continue

                val flags = sbn.notification.flags
                val rawActions = sbn.notification.actions.orEmpty().mapIndexed { idx, action ->
                    RawNotificationAction(
                        originalIndex = idx,
                        title = action.title?.toString(),
                        hasIntent = action.actionIntent != null,
                        hasRemoteInput = !action.remoteInputs.isNullOrEmpty(),
                        isReplySemanticAction = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                            action.semanticAction == Notification.Action.SEMANTIC_ACTION_REPLY
                    )
                }
                val isGroupSummary = (flags and Notification.FLAG_GROUP_SUMMARY) != 0
                val item = AppNotificationItem(
                    key = sbn.key,
                    packageName = pkg,
                    title = title,
                    text = text,
                    postTime = sbn.postTime,
                    isClearable = sbn.isClearable,
                    actions = NotificationStateUtils.filterOneTapActions(rawActions, maxCount = 3),
                    isGroupSummary = isGroupSummary
                )

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
