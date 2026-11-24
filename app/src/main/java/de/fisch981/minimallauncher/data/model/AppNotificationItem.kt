package de.fisch981.minimallauncher.data.model

data class NotificationActionItem(
    val index: Int,
    val title: String
)

data class AppNotificationItem(
    val key: String,
    val packageName: String,
    val title: String?,
    val text: String?,
    val postTime: Long,
    val isClearable: Boolean = true,
    val actions: List<NotificationActionItem> = emptyList(),
    val isGroupSummary: Boolean = false
)
