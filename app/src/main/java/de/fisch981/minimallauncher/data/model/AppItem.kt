package de.fisch981.minimallauncher.data.model

import android.graphics.Bitmap
import android.os.UserHandle

data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val userHandle: UserHandle,
    val icon: Bitmap? = null,
    val isFavorite: Boolean = false,
    val subApps: List<AppItem> = emptyList(),
    val isHidden: Boolean = false,
    val originalLabel: String = label
) {
    val key: String get() = "$packageName/$activityName#${userHandle.hashCode()}"
    val initialChar: Char get() {
        val first = label.trim().firstOrNull()?.uppercaseChar() ?: '#'
        return if (first in 'A'..'Z') first else '#'
    }
}
