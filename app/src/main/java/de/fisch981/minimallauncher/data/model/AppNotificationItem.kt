package de.fisch981.minimallauncher.data.model

data class AppNotificationItem(
    val key: String,
    val packageName: String,
    val title: String?,
    val text: String?,
    val postTime: Long
)
