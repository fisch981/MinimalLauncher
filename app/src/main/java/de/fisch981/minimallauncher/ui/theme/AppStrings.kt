package de.fisch981.minimallauncher.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

enum class AppLanguage(val code: String) {
    SYSTEM("system"),
    DE("de"),
    EN("en");

    fun isGerman(locale: Locale = Locale.getDefault()): Boolean = when (this) {
        DE -> true
        EN -> false
        SYSTEM -> locale.language.equals("de", ignoreCase = true)
    }

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
    }
}

data class AppStrings(
    val isGerman: Boolean,

    // Common
    val back: String,
    val close: String,
    val done: String,
    val cancel: String,
    val save: String,
    val reset: String,
    val delete: String,
    val search: String,

    // HomeScreen
    val searchAppsButton: String,
    val settingsContentDesc: String,
    val editFavorites: String,
    val launcherSettings: String,
    val recentlyUsed: String,
    val collapse: String,
    val expand: String,
    val folderForFormat: String,
    val selectedIconReadError: String,

    // HeaderWidget
    val dateFormatPattern: String,
    val dateLocale: Locale,
    val unknownTitle: String,
    val mediaPrevious: String,
    val mediaPlayPause: String,
    val mediaNext: String,

    // AppPopupSheet
    val folderSubAppsHeader: String,
    val shortcutsHeader: String,
    val notificationsHeader: String,
    val removeFromFavorites: String,
    val addToFavorites: String,
    val renameAppAction: String,
    val changeIconAction: String,
    val restoreDefaultIconAction: String,
    val addSubAppToFolderAction: String,
    val hideAppAction: String,
    val systemAppDetailsAction: String,

    // FavoritesSelectionDialog
    val selectFavoritesTitle: String,
    val appsSelectedFormat: String,

    // IconPickerDialog
    val changeIconForHeader: String,
    val searchIconsPlaceholder: String,

    // RenameAppDialog
    val renameAppTitle: String,
    val originalNameFormat: String,

    // SearchOverlay
    val searchAppsPlaceholder: String,
    val settingsTitle: String,
    val settingsSearchSubtitle: String,

    // SettingsScreen
    val sectionDefaultApp: String,
    val defaultLauncherTitle: String,
    val defaultLauncherActive: String,
    val defaultLauncherInactive: String,
    val changeButton: String,
    val setButton: String,
    val sectionBackupRestore: String,
    val exportBackupTitle: String,
    val exportBackupSubtitle: String,
    val importBackupTitle: String,
    val importBackupSubtitle: String,
    val backupExportSuccessToast: String,
    val backupExportErrorFormat: String,
    val backupImportSuccessToast: String,
    val backupImportErrorFormat: String,
    val sectionNovaImporter: String,
    val selectNovaBackupTitle: String,
    val novaCustomMappingsActiveFormat: String,
    val novaBackupSubtitleDefault: String,
    val resetNovaMappingsTitle: String,
    val resetNovaMappingsSubtitleFormat: String,
    val novaMappingsResetToast: String,
    val novaImportSuccessFormat: String,
    val sectionFavorites: String,
    val manageFavoritesTitle: String,
    val manageFavoritesSubtitleFormat: String,
    val showRecentAppsTitle: String,
    val showRecentAppsSubtitle: String,
    val sectionAppearanceWave: String,
    val languageTitle: String,
    val languageSubtitle: String,
    val languageSystem: String,
    val languageGerman: String,
    val languageEnglish: String,
    val wavePositionTitle: String,
    val wavePositionSubtitle: String,
    val sideLeft: String,
    val sideRight: String,
    val sideBoth: String,
    val waveIdleBrightnessTitle: String,
    val waveIdleBrightnessSubtitle: String,
    val linesPackStatusTitle: String,
    val linesPackActiveSubtitle: String,
    val linesPackInactiveSubtitle: String,
    val sectionUsageStats: String,
    val waveUsageNone: String,
    val waveUsageLeftMoreFormat: String,
    val waveUsageRightMoreFormat: String,
    val waveUsageEqual: String,
    val waveSideUsageTitle: String,
    val mostLaunchedAppsTitle: String,
    val mostLaunchedAppsSubtitle: String,
    val noAppLaunchesRecorded: String,
    val launchedCountFormat: String,
    val favoriteBadgeActive: String,
    val favoriteBadgeAdd: String,
    val resetStatsButton: String,
    val sectionWidgetsNotifications: String,
    val showBatteryTitle: String,
    val showBatterySubtitle: String,
    val showMediaTitle: String,
    val showMediaSubtitle: String,
    val notificationAccessTitle: String,
    val notificationAccessGranted: String,
    val notificationAccessDenied: String,
    val sectionHiddenAppsFormat: String,
    val noHiddenApps: String,
    val unhideAppContentDesc: String
) {
    fun folderForTitle(appLabel: String): String = String.format(dateLocale, folderForFormat, appLabel)
    fun appsSelectedSubtitle(count: Int): String = String.format(dateLocale, appsSelectedFormat, count)
    fun originalNameSubtitle(original: String): String = String.format(dateLocale, originalNameFormat, original)
    fun novaCustomMappingsActive(count: Int): String = String.format(dateLocale, novaCustomMappingsActiveFormat, count)
    fun resetNovaMappingsSubtitle(count: Int): String = String.format(dateLocale, resetNovaMappingsSubtitleFormat, count)
    fun novaImportSuccessToast(count: Int): String = String.format(dateLocale, novaImportSuccessFormat, count)
    fun manageFavoritesSubtitle(count: Int): String = String.format(dateLocale, manageFavoritesSubtitleFormat, count)
    fun waveUsageLeftMore(percent: Int): String = String.format(dateLocale, waveUsageLeftMoreFormat, percent)
    fun waveUsageRightMore(percent: Int): String = String.format(dateLocale, waveUsageRightMoreFormat, percent)
    fun launchedCountSubtitle(count: Int): String = String.format(dateLocale, launchedCountFormat, count)
    fun sectionHiddenApps(count: Int): String = String.format(dateLocale, sectionHiddenAppsFormat, count)
    fun backupExportErrorToast(error: String): String = String.format(dateLocale, backupExportErrorFormat, error)
    fun backupImportErrorToast(error: String): String = String.format(dateLocale, backupImportErrorFormat, error)
}

val GermanStrings = AppStrings(
    isGerman = true,

    back = "Zurück",
    close = "Schließen",
    done = "Fertig",
    cancel = "Abbrechen",
    save = "Speichern",
    reset = "Zurücksetzen",
    delete = "Löschen",
    search = "Suche",

    searchAppsButton = "Suche",
    settingsContentDesc = "Einstellungen",
    editFavorites = "Favoriten bearbeiten",
    launcherSettings = "Launcher-Einstellungen",
    recentlyUsed = "Zuletzt benutzt",
    collapse = "Einklappen",
    expand = "Ausklappen",
    folderForFormat = "Ordner für %s",
    selectedIconReadError = "Ausgewähltes Icon konnte nicht gelesen werden.",

    dateFormatPattern = "EEEE, d. MMMM",
    dateLocale = Locale.GERMAN,
    unknownTitle = "Titel unbekannt",
    mediaPrevious = "Vorheriger",
    mediaPlayPause = "Play/Pause",
    mediaNext = "Nächster",

    folderSubAppsHeader = "Ordner / Unter-Apps",
    shortcutsHeader = "Shortcuts",
    notificationsHeader = "Benachrichtigungen",
    removeFromFavorites = "Aus Favoriten entfernen",
    addToFavorites = "Zu Favoriten hinzufügen",
    renameAppAction = "Name ändern",
    changeIconAction = "Icon ändern",
    restoreDefaultIconAction = "Standard-Icon wiederherstellen",
    addSubAppToFolderAction = "Unter-App zu Ordner hinzufügen",
    hideAppAction = "App verbergen",
    systemAppDetailsAction = "App-Details im System",

    selectFavoritesTitle = "Favoriten verwalten",
    appsSelectedFormat = "%d Apps ausgewählt",

    changeIconForHeader = "Icon ändern für:",
    searchIconsPlaceholder = "Icons durchsuchen (z.B. camera, chat, music)...",

    renameAppTitle = "App umbenennen",
    originalNameFormat = "Original: %s",

    searchAppsPlaceholder = "Apps durchsuchen...",
    settingsTitle = "MinimalLauncher-Einstellungen",
    settingsSearchSubtitle = "Nova Backup importieren, Favoriten, Design",

    sectionDefaultApp = "Standard-App",
    defaultLauncherTitle = "Standard-Launcher",
    defaultLauncherActive = "MinimalLauncher ist aktiv",
    defaultLauncherInactive = "Tippen zum Aktivieren",
    changeButton = "Ändern",
    setButton = "Festlegen",
    sectionBackupRestore = "Sicherung & Wiederherstellung",
    exportBackupTitle = "Einstellungen sichern (.json)",
    exportBackupSubtitle = "Speichert Favoriten, Ordner, eigene Namen, Icons und Einstellungen",
    importBackupTitle = "Sicherung wiederherstellen (.json)",
    importBackupSubtitle = "Lädt eine zuvor gespeicherte MinimalLauncher-Sicherungsdatei",
    backupExportSuccessToast = "Sicherung erfolgreich gespeichert",
    backupExportErrorFormat = "Sicherung fehlgeschlagen: %s",
    backupImportSuccessToast = "Sicherung erfolgreich wiederhergestellt",
    backupImportErrorFormat = "Wiederherstellung fehlgeschlagen: %s",
    sectionNovaImporter = "Nova Launcher Backup Importer",
    selectNovaBackupTitle = "Backup-Datei auswählen (.novabackup)",
    novaCustomMappingsActiveFormat = "%d benutzerdefinierte Zuordnungen aktiv (tippen zum erneuten Import)",
    novaBackupSubtitleDefault = "Überträgt alle manuell zugewiesenen Icons direkt aus deiner Nova-Backup-Datei",
    resetNovaMappingsTitle = "Importierte Zuordnungen zurücksetzen",
    resetNovaMappingsSubtitleFormat = "Entfernt alle %d aus Nova importierten Icon-Zuweisungen",
    novaMappingsResetToast = "Zuordnungen zurückgesetzt",
    novaImportSuccessFormat = "%d Icon-Zuordnungen erfolgreich aus Nova Launcher importiert!",
    sectionFavorites = "Favoriten",
    manageFavoritesTitle = "Favoriten verwalten",
    manageFavoritesSubtitleFormat = "%d Apps ausgewählt (auf dem Homescreen angezeigt)",
    showRecentAppsTitle = "Zuletzt benutzte Apps anzeigen",
    showRecentAppsSubtitle = "Die 5 zuletzt geöffneten Apps auf der Favoriten-Seite einblenden",
    sectionAppearanceWave = "Erscheinungsbild & Wave Scroller",
    languageTitle = "Sprache",
    languageSubtitle = "Systemsprache (Fallback Englisch) oder manuelle Auswahl",
    languageSystem = "System",
    languageGerman = "Deutsch",
    languageEnglish = "English",
    wavePositionTitle = "Position der Alphabet-Welle",
    wavePositionSubtitle = "Wahlweise links, rechts oder auf beiden Seiten einblenden",
    sideLeft = "Links",
    sideRight = "Rechts",
    sideBoth = "Beidseitig",
    waveIdleBrightnessTitle = "Helligkeit der Buchstaben im Ruhezustand",
    waveIdleBrightnessSubtitle = "Bei Benutzung sofort 100% hell, danach nach 1 Sek. Verzögerung sanft ausfaden",
    linesPackStatusTitle = "Lines Icon-Pack Status",
    linesPackActiveSubtitle = "Lines Pack aktiv (weiße Konturen)",
    linesPackInactiveSubtitle = "Nicht installiert - automatischer Schwarz-Weiß-Filter aktiv",
    sectionUsageStats = "Nutzungsstatistik",
    waveUsageNone = "Noch keine Wellen-Interaktionen aufgezeichnet",
    waveUsageLeftMoreFormat = "Linke Welle wird häufiger genutzt (%d%%)",
    waveUsageRightMoreFormat = "Rechte Welle wird häufiger genutzt (%d%%)",
    waveUsageEqual = "Beide Seiten werden gleich häufig genutzt (50% / 50%)",
    waveSideUsageTitle = "Alphabet-Welle: Seiten-Nutzung",
    mostLaunchedAppsTitle = "Am häufigsten gestartete Apps",
    mostLaunchedAppsSubtitle = "Meistgenutzte Apps direkt zu den Favoriten hinzufügen",
    noAppLaunchesRecorded = "Noch keine App-Starts aufgezeichnet",
    launchedCountFormat = "%d× gestartet",
    favoriteBadgeActive = "★ Favorit",
    favoriteBadgeAdd = "+ Favorit",
    resetStatsButton = "Statistik zurücksetzen",
    sectionWidgetsNotifications = "Widgets & Benachrichtigungen",
    showBatteryTitle = "Akku im Header anzeigen",
    showBatterySubtitle = "Akkustand neben dem Datum einblenden",
    showMediaTitle = "Medienplayer anzeigen",
    showMediaSubtitle = "Steuerung aktiver Musikwiedergabe im Header",
    notificationAccessTitle = "Benachrichtigungszugriff",
    notificationAccessGranted = "Aktiviert (Vorschautexte & Mediensteuerung aktiv)",
    notificationAccessDenied = "Tippen, um in Android-Einstellungen zu erlauben",
    sectionHiddenAppsFormat = "Ausgeblendete Apps (%d)",
    noHiddenApps = "Keine ausgeblendeten Apps",
    unhideAppContentDesc = "Wieder anzeigen"
)

val EnglishStrings = AppStrings(
    isGerman = false,

    back = "Back",
    close = "Close",
    done = "Done",
    cancel = "Cancel",
    save = "Save",
    reset = "Reset",
    delete = "Clear",
    search = "Search",

    searchAppsButton = "Search",
    settingsContentDesc = "Settings",
    editFavorites = "Edit favorites",
    launcherSettings = "Launcher settings",
    recentlyUsed = "Recently used",
    collapse = "Collapse",
    expand = "Expand",
    folderForFormat = "Folder for %s",
    selectedIconReadError = "Selected icon could not be read.",

    dateFormatPattern = "EEEE, MMMM d",
    dateLocale = Locale.ENGLISH,
    unknownTitle = "Unknown title",
    mediaPrevious = "Previous",
    mediaPlayPause = "Play/Pause",
    mediaNext = "Next",

    folderSubAppsHeader = "Folder / Sub-apps",
    shortcutsHeader = "Shortcuts",
    notificationsHeader = "Notifications",
    removeFromFavorites = "Remove from favorites",
    addToFavorites = "Add to favorites",
    renameAppAction = "Rename app",
    changeIconAction = "Change icon",
    restoreDefaultIconAction = "Restore default icon",
    addSubAppToFolderAction = "Add sub-app to folder",
    hideAppAction = "Hide app",
    systemAppDetailsAction = "System app info",

    selectFavoritesTitle = "Manage favorites",
    appsSelectedFormat = "%d apps selected",

    changeIconForHeader = "Change icon for:",
    searchIconsPlaceholder = "Search icons (e.g. camera, chat, music)...",

    renameAppTitle = "Rename app",
    originalNameFormat = "Original: %s",

    searchAppsPlaceholder = "Search apps...",
    settingsTitle = "MinimalLauncher Settings",
    settingsSearchSubtitle = "Import Nova backup, favorites, appearance",

    sectionDefaultApp = "Default App",
    defaultLauncherTitle = "Default Launcher",
    defaultLauncherActive = "MinimalLauncher is active",
    defaultLauncherInactive = "Tap to activate",
    changeButton = "Change",
    setButton = "Set",
    sectionBackupRestore = "Backup & Restore",
    exportBackupTitle = "Export backup (.json)",
    exportBackupSubtitle = "Saves favorites, folders, custom names, icons, and settings",
    importBackupTitle = "Restore backup (.json)",
    importBackupSubtitle = "Loads a previously saved MinimalLauncher backup file",
    backupExportSuccessToast = "Backup saved successfully",
    backupExportErrorFormat = "Backup export failed: %s",
    backupImportSuccessToast = "Backup restored successfully",
    backupImportErrorFormat = "Backup restore failed: %s",
    sectionNovaImporter = "Nova Launcher Backup Importer",
    selectNovaBackupTitle = "Select backup file (.novabackup)",
    novaCustomMappingsActiveFormat = "%d custom mappings active (tap to re-import)",
    novaBackupSubtitleDefault = "Transfers all manually assigned icons directly from your Nova backup file",
    resetNovaMappingsTitle = "Reset imported mappings",
    resetNovaMappingsSubtitleFormat = "Removes all %d icon mappings imported from Nova",
    novaMappingsResetToast = "Mappings reset",
    novaImportSuccessFormat = "%d icon mappings successfully imported from Nova Launcher!",
    sectionFavorites = "Favorites",
    manageFavoritesTitle = "Manage favorites",
    manageFavoritesSubtitleFormat = "%d apps selected (shown on home screen)",
    showRecentAppsTitle = "Show recently used apps",
    showRecentAppsSubtitle = "Show the 5 most recently opened apps on the favorites page",
    sectionAppearanceWave = "Appearance & Wave Scroller",
    languageTitle = "Language",
    languageSubtitle = "System language (English fallback) or manual selection",
    languageSystem = "System",
    languageGerman = "Deutsch",
    languageEnglish = "English",
    wavePositionTitle = "Alphabet wave position",
    wavePositionSubtitle = "Show on the left, right, or both sides",
    sideLeft = "Left",
    sideRight = "Right",
    sideBoth = "Both sides",
    waveIdleBrightnessTitle = "Letter brightness when idle",
    waveIdleBrightnessSubtitle = "Instantly 100% bright on touch, then smoothly fades out after a 1 sec delay",
    linesPackStatusTitle = "Lines Icon Pack status",
    linesPackActiveSubtitle = "Lines pack active (white outlines)",
    linesPackInactiveSubtitle = "Not installed - automatic monochrome filter active",
    sectionUsageStats = "Usage Statistics",
    waveUsageNone = "No wave interactions recorded yet",
    waveUsageLeftMoreFormat = "Left wave is used more often (%d%%)",
    waveUsageRightMoreFormat = "Right wave is used more often (%d%%)",
    waveUsageEqual = "Both sides are used equally (50% / 50%)",
    waveSideUsageTitle = "Alphabet wave: side usage",
    mostLaunchedAppsTitle = "Most frequently launched apps",
    mostLaunchedAppsSubtitle = "Add your most used apps directly to favorites",
    noAppLaunchesRecorded = "No app launches recorded yet",
    launchedCountFormat = "Launched %d×",
    favoriteBadgeActive = "★ Favorite",
    favoriteBadgeAdd = "+ Favorite",
    resetStatsButton = "Reset statistics",
    sectionWidgetsNotifications = "Widgets & Notifications",
    showBatteryTitle = "Show battery in header",
    showBatterySubtitle = "Display battery level next to the date",
    showMediaTitle = "Show media player",
    showMediaSubtitle = "Control active music playback in the header",
    notificationAccessTitle = "Notification access",
    notificationAccessGranted = "Enabled (preview texts & media controls active)",
    notificationAccessDenied = "Tap to allow in Android settings",
    sectionHiddenAppsFormat = "Hidden Apps (%d)",
    noHiddenApps = "No hidden apps",
    unhideAppContentDesc = "Unhide app"
)

val LocalAppStrings = staticCompositionLocalOf { EnglishStrings }
