package de.fisch981.minimallauncher

import de.fisch981.minimallauncher.data.backup.LauncherBackupCodec
import de.fisch981.minimallauncher.data.backup.LauncherBackupData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherBackupCodecTest {

    @Test
    fun roundTrip_preservesAllSettingsAndSpecialCharacters() {
        val original = LauncherBackupData(
            favorites = listOf(
                "com.example.camera/com.example.camera.MainActivity#0",
                "com.example.notes/com.example.notes.NotesActivity#0"
            ),
            hiddenApps = setOf("com.example.hidden/com.example.hidden.Launcher#0"),
            popupFolders = mapOf(
                "com.example.camera/com.example.camera.MainActivity#0" to listOf(
                    "com.example.gallery/com.example.gallery.Main#0",
                    "com.example.editor/com.example.editor.Edit#0"
                )
            ),
            scrollerSide = "BOTH",
            waveIdleAlphaPercent = 25,
            appLanguage = "de",
            appFont = "serif",
            clockDateFont = "condensed",
            waveFont = "monospace",
            appFontSizeSp = 19,
            dateSizeSp = 13,
            waveFontSizeSp = 12,
            showClock = false,
            clockSizeSp = 68,
            showDate = false,
            showMedia = false,
            showBattery = true,
            showRecentApps = false,
            showAppIcons = false,
            hideStatusBar = true,
            showBottomActions = false,
            showNotificationPreview = false,
            recentApps = listOf("com.example.browser/com.example.browser.Main#0"),
            leftWaveCount = 14,
            rightWaveCount = 29,
            appLaunchCounts = mapOf(
                "com.example.camera/com.example.camera.MainActivity#0" to 42,
                "com.example.notes/com.example.notes.NotesActivity#0" to 7
            ),
            customIcons = mapOf(
                "com.example.camera/com.example.camera.MainActivity#0" to "camera_alt"
            ),
            customNames = mapOf(
                "com.example.notes/com.example.notes.NotesActivity#0" to "Notizen \"Privat\" \\ Bücher"
            ),
            importedIconFiles = mapOf(
                "p_com.example.camera.png" to "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
            )
        )

        val json = LauncherBackupCodec.encodeToJson(original)
        val decoded = LauncherBackupCodec.decodeFromJson(json)

        assertEquals(LauncherBackupCodec.FORMAT_ID, decoded.format)
        assertEquals(LauncherBackupCodec.CURRENT_VERSION, decoded.version)
        assertEquals(original.favorites, decoded.favorites)
        assertEquals(original.hiddenApps, decoded.hiddenApps)
        assertEquals(original.popupFolders, decoded.popupFolders)
        assertEquals("BOTH", decoded.scrollerSide)
        assertEquals(25, decoded.waveIdleAlphaPercent)
        assertEquals("de", decoded.appLanguage)
        assertEquals("serif", decoded.appFont)
        assertEquals("condensed", decoded.clockDateFont)
        assertEquals("monospace", decoded.waveFont)
        assertEquals(19, decoded.appFontSizeSp)
        assertEquals(13, decoded.dateSizeSp)
        assertEquals(12, decoded.waveFontSizeSp)
        assertFalse(decoded.showClock)
        assertEquals(68, decoded.clockSizeSp)
        assertFalse(decoded.showDate)
        assertFalse(decoded.showMedia)
        assertTrue(decoded.showBattery)
        assertFalse(decoded.showRecentApps)
        assertFalse(decoded.showAppIcons)
        assertTrue(decoded.hideStatusBar)
        assertFalse(decoded.showBottomActions)
        assertFalse(decoded.showNotificationPreview)
        assertEquals(original.recentApps, decoded.recentApps)
        assertEquals(14, decoded.leftWaveCount)
        assertEquals(29, decoded.rightWaveCount)
        assertEquals(original.appLaunchCounts, decoded.appLaunchCounts)
        assertEquals(original.customIcons, decoded.customIcons)
        assertEquals(original.customNames, decoded.customNames)
        assertEquals(original.importedIconFiles, decoded.importedIconFiles)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decode_rejectsWrongFormat() {
        LauncherBackupCodec.decodeFromJson("""{"format": "other-launcher", "version": 1}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun decode_rejectsMalformedJson() {
        LauncherBackupCodec.decodeFromJson("""{"format": "minimallauncher-backup", """)
    }
}
