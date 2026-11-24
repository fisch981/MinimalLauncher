package de.fisch981.minimallauncher

import de.fisch981.minimallauncher.data.model.AppNotificationItem
import de.fisch981.minimallauncher.data.service.NotificationStateUtils
import de.fisch981.minimallauncher.data.service.RankedNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationStateUtilsTest {

    @Test
    fun testSortPackageNotificationsPrefersNonSummaryNewestFirst() {
        val summaryNewest = RankedNotification(
            item = AppNotificationItem("summary", "com.chat", "Chat", "3 messages", postTime = 3000L),
            isGroupSummary = true
        )
        val msgOlder = RankedNotification(
            item = AppNotificationItem("m1", "com.chat", "Alice", "Hello", postTime = 1000L),
            isGroupSummary = false
        )
        val msgNewest = RankedNotification(
            item = AppNotificationItem("m2", "com.chat", "Bob", "New photo", postTime = 2000L),
            isGroupSummary = false
        )

        val sorted = NotificationStateUtils.sortPackageNotifications(listOf(summaryNewest, msgOlder, msgNewest))
        assertEquals(listOf("m2", "m1", "summary"), sorted.map { it.key })
    }

    @Test
    fun testRemoveDismissedKeysRemovesPackageWhenEmpty() {
        val initial = mapOf(
            "com.chat" to listOf(
                AppNotificationItem("k1", "com.chat", "Alice", "Hi", 100L),
                AppNotificationItem("k2", "com.chat", "Bob", "Hey", 200L)
            ),
            "com.mail" to listOf(
                AppNotificationItem("m1", "com.mail", "Inbox", "Subject", 150L)
            )
        )

        val updated = NotificationStateUtils.removeDismissedKeys(initial, "com.chat", setOf("k1", "k2"))
        assertFalse(updated.containsKey("com.chat"))
        assertTrue(updated.containsKey("com.mail"))
        assertEquals(1, updated["com.mail"]?.size)
    }

    @Test
    fun testRemoveDismissedKeysKeepsRemainingNonDismissedItems() {
        val initial = mapOf(
            "com.chat" to listOf(
                AppNotificationItem("clearable", "com.chat", "Alice", "Hi", 100L, isClearable = true),
                AppNotificationItem("pinned", "com.chat", "Call", "Ongoing", 200L, isClearable = false)
            )
        )

        val updated = NotificationStateUtils.removeDismissedKeys(initial, "com.chat", setOf("clearable"))
        assertEquals(listOf("pinned"), updated["com.chat"]?.map { it.key })
    }

    @Test
    fun testShouldTrackNotificationFiltersMediaAndNonClearable() {
        // Standard clearable notification
        assertTrue(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "com.chat",
                isOngoing = false,
                isForegroundService = false,
                isClearable = true,
                isNoClear = false,
                hasMediaSession = false,
                category = "msg",
                template = null,
                isLowImportance = false,
                allowDismissibleOngoing = true
            )
        )

        // Upcoming alarm (Clock posts CATEGORY_ALARM with FLAG_ONGOING_EVENT -> isClearable = false)
        assertTrue(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "com.google.android.deskclock",
                isOngoing = true,
                isForegroundService = false,
                isClearable = false,
                isNoClear = false,
                hasMediaSession = false,
                category = "alarm",
                template = null,
                isLowImportance = false,
                allowDismissibleOngoing = true
            )
        )
        assertTrue(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "com.google.android.deskclock",
                isOngoing = true,
                isForegroundService = false,
                isClearable = false,
                isNoClear = false,
                hasMediaSession = false,
                category = "alarm",
                template = null,
                isLowImportance = false,
                allowDismissibleOngoing = false
            )
        )

        // Android 14+ non-FGS ongoing app notification without special category
        assertTrue(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "com.example.timer",
                isOngoing = true,
                isForegroundService = false,
                isClearable = false,
                isNoClear = false,
                hasMediaSession = false,
                category = null,
                template = null,
                isLowImportance = false,
                allowDismissibleOngoing = true
            )
        )
        assertFalse(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "com.example.timer",
                isOngoing = true,
                isForegroundService = false,
                isClearable = false,
                isNoClear = false,
                hasMediaSession = false,
                category = null,
                template = null,
                isLowImportance = false,
                allowDismissibleOngoing = false
            )
        )

        // Upcoming alarm (even if flagged ongoing, FGS, or low-importance) stays tracked
        assertTrue(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "com.google.android.deskclock",
                isOngoing = true,
                isForegroundService = true,
                isClearable = false,
                isNoClear = true,
                hasMediaSession = false,
                category = "alarm",
                template = null,
                isLowImportance = true,
                allowDismissibleOngoing = false
            )
        )

        // Persistent foreground service (non-alarm) stays filtered out even on API 34+
        assertFalse(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "com.example.vpn",
                isOngoing = true,
                isForegroundService = true,
                isClearable = false,
                isNoClear = false,
                hasMediaSession = false,
                category = "service",
                template = null,
                isLowImportance = false,
                allowDismissibleOngoing = true
            )
        )

        // Media notifications (Pocket Casts, Spotify, etc.) stay excluded
        assertFalse(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "au.com.shiftyjelly.pocketcasts",
                isOngoing = false,
                isForegroundService = false,
                isClearable = true,
                isNoClear = false,
                hasMediaSession = true,
                category = null,
                template = null,
                isLowImportance = false,
                allowDismissibleOngoing = true
            )
        )
        assertFalse(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "au.com.shiftyjelly.pocketcasts",
                isOngoing = false,
                isForegroundService = false,
                isClearable = true,
                isNoClear = false,
                hasMediaSession = false,
                category = "transport",
                template = null,
                isLowImportance = false,
                allowDismissibleOngoing = true
            )
        )
        assertFalse(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "au.com.shiftyjelly.pocketcasts",
                isOngoing = false,
                isForegroundService = false,
                isClearable = true,
                isNoClear = false,
                hasMediaSession = false,
                category = null,
                template = "android.app.Notification\$MediaStyle",
                isLowImportance = false,
                allowDismissibleOngoing = true
            )
        )

        // System UI ongoing / service / low-importance chrome stays excluded
        assertFalse(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "android",
                isOngoing = true,
                isForegroundService = false,
                isClearable = false,
                isNoClear = false,
                hasMediaSession = false,
                category = null,
                template = null,
                isLowImportance = false,
                allowDismissibleOngoing = true
            )
        )
        assertFalse(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "com.android.systemui",
                isOngoing = true,
                isForegroundService = false,
                isClearable = false,
                isNoClear = false,
                hasMediaSession = false,
                category = null,
                template = null,
                isLowImportance = false,
                allowDismissibleOngoing = true
            )
        )
        assertFalse(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "com.example.bg",
                isOngoing = true,
                isForegroundService = false,
                isClearable = false,
                isNoClear = false,
                hasMediaSession = false,
                category = "service",
                template = null,
                isLowImportance = false,
                allowDismissibleOngoing = true
            )
        )
        assertFalse(
            NotificationStateUtils.shouldTrackNotification(
                packageName = "com.example.ambient",
                isOngoing = true,
                isForegroundService = false,
                isClearable = false,
                isNoClear = false,
                hasMediaSession = false,
                category = null,
                template = null,
                isLowImportance = true,
                allowDismissibleOngoing = true
            )
        )
    }

    @Test
    fun testLocalDismissalSuppressionAndNewerReappearance() {
        val dismissed = mapOf("k1" to 1000L, "k2" to 2000L)

        // Same or older postTime stays suppressed; newer postTime reappears
        val activeItems = listOf(
            RankedNotification(AppNotificationItem("k1", "pkg", "Title", "Old", 1000L), false),
            RankedNotification(AppNotificationItem("k2", "pkg", "Title", "Updated", 2500L), false)
        )
        val filtered = NotificationStateUtils.filterLocallyDismissed(activeItems, dismissed)
        assertEquals(listOf("k2"), filtered.map { it.item.key })

        // Prune drops keys no longer active or overwritten by newer postTime
        val pruned = NotificationStateUtils.pruneDismissedKeys(
            dismissedKeyPostTimes = dismissed,
            liveActiveKeysWithPostTime = mapOf("k1" to 1000L, "k2" to 2500L)
        )
        assertEquals(mapOf("k1" to 1000L), pruned)
    }
}
