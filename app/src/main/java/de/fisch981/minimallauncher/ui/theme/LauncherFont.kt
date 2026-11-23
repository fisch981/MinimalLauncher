package de.fisch981.minimallauncher.ui.theme

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily

enum class LauncherFont(val code: String) {
    SYSTEM("system"),
    CONDENSED("condensed"),
    THIN("thin"),
    SERIF("serif"),
    MONOSPACE("monospace"),
    CURSIVE("cursive");

    fun toFontFamily(): FontFamily = when (this) {
        SYSTEM -> FontFamily.SansSerif
        CONDENSED -> FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
        THIN -> FontFamily(Typeface.create("sans-serif-thin", Typeface.NORMAL))
        SERIF -> FontFamily.Serif
        MONOSPACE -> FontFamily.Monospace
        CURSIVE -> FontFamily.Cursive
    }

    companion object {
        fun fromCode(code: String?): LauncherFont =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
    }
}
