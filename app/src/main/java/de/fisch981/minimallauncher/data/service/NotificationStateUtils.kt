package de.fisch981.minimallauncher.data.service

import de.fisch981.minimallauncher.data.model.AppNotificationItem

internal data class RankedNotification(
    val item: AppNotificationItem,
    val isGroupSummary: Boolean
)

internal object NotificationStateUtils {
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
