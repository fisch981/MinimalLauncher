package de.fisch981.minimallauncher.data.service

import de.fisch981.minimallauncher.data.model.AppNotificationItem

internal data class RankedNotification(
    val item: AppNotificationItem,
    val isGroupSummary: Boolean
)

internal object NotificationStateUtils {
    private val SYSTEM_NOISE_CATEGORIES = setOf("service", "sys", "status")
    private val USER_ACTIONABLE_ONGOING_CATEGORIES = setOf("alarm", "reminder", "event", "call")
    private val SYSTEM_NOISE_PACKAGES = setOf("android", "com.android.systemui")

    /**
     * Returns true when a notification represents an active or resumable media playback control,
     * which Android may keep in activeNotifications even after the user swipes it away.
     */
    fun isMediaNotification(
        hasMediaSession: Boolean,
        category: String?,
        template: String?
    ): Boolean {
        if (hasMediaSession) return true
        if (category.equals("transport", ignoreCase = true)) return true
        return template?.contains("MediaStyle", ignoreCase = true) == true
    }

    /**
     * Tracks normal clearable notifications as well as user-actionable or Android 14+ dismissible
     * non-FGS ongoing app notifications (such as upcoming alarms), while excluding media controls,
     * foreground services, low-importance ambient items, and system UI chrome.
     */
    fun shouldTrackNotification(
        packageName: String,
        isOngoing: Boolean,
        isForegroundService: Boolean,
        isClearable: Boolean,
        isNoClear: Boolean,
        hasMediaSession: Boolean,
        category: String?,
        template: String?,
        isLowImportance: Boolean,
        allowDismissibleOngoing: Boolean
    ): Boolean {
        if (isMediaNotification(hasMediaSession, category, template)) return false
        if (isForegroundService) return false
        if (!isOngoing && isClearable && !isNoClear) return true
        if (isLowImportance) return false
        if (packageName in SYSTEM_NOISE_PACKAGES) return false
        val normalizedCategory = category?.lowercase()
        if (normalizedCategory in SYSTEM_NOISE_CATEGORIES) return false
        if (normalizedCategory in USER_ACTIONABLE_ONGOING_CATEGORIES) return true
        return allowDismissibleOngoing && isOngoing && !isNoClear
    }

    /**
     * Suppresses notifications the user already dismissed in the launcher unless a newer postTime
     * has arrived for the same key.
     */
    fun filterLocallyDismissed(
        items: List<RankedNotification>,
        dismissedKeyPostTimes: Map<String, Long>
    ): List<RankedNotification> {
        if (dismissedKeyPostTimes.isEmpty()) return items
        return items.filterNot { ranked ->
            val dismissedPostTime = dismissedKeyPostTimes[ranked.item.key] ?: return@filterNot false
            ranked.item.postTime <= dismissedPostTime
        }
    }

    /**
     * Drops locally dismissed keys once Android removes the notification or posts a newer update.
     */
    fun pruneDismissedKeys(
        dismissedKeyPostTimes: Map<String, Long>,
        liveActiveKeysWithPostTime: Map<String, Long>
    ): Map<String, Long> {
        if (dismissedKeyPostTimes.isEmpty()) return emptyMap()
        return dismissedKeyPostTimes.filter { (key, dismissedPostTime) ->
            val livePostTime = liveActiveKeysWithPostTime[key] ?: return@filter false
            livePostTime <= dismissedPostTime
        }
    }

    /**
     * Places individual notifications before group summaries and orders each group newest-first.
     */
    fun sortPackageNotifications(items: List<RankedNotification>): List<AppNotificationItem> {
        return items
            .sortedWith(
                compareBy<RankedNotification> { it.isGroupSummary }
                    .thenByDescending { it.item.postTime }
            )
            .map { it.item }
    }

    /**
     * Removes [dismissedKeys] from [packageName]'s active notifications and drops the entry
     * altogether when no notifications remain.
     */
    fun removeDismissedKeys(
        current: Map<String, List<AppNotificationItem>>,
        packageName: String,
        dismissedKeys: Set<String>
    ): Map<String, List<AppNotificationItem>> {
        if (dismissedKeys.isEmpty() || !current.containsKey(packageName)) return current
        val remaining = current[packageName].orEmpty().filterNot { it.key in dismissedKeys }
        val updated = current.toMutableMap()
        if (remaining.isEmpty()) {
            updated.remove(packageName)
        } else {
            updated[packageName] = remaining
        }
        return updated
    }
}
