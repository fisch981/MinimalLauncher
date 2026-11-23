package de.fisch981.minimallauncher.data.model

import android.content.pm.ShortcutInfo
import android.graphics.Bitmap

data class ShortcutItem(
    val id: String,
    val packageName: String,
    val shortLabel: String,
    val longLabel: String? = null,
    val icon: Bitmap? = null,
    val rawShortcutInfo: ShortcutInfo? = null
)
