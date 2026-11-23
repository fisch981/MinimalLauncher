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
}
