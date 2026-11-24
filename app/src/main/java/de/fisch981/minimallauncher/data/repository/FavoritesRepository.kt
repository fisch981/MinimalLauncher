package de.fisch981.minimallauncher.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.fisch981.minimallauncher.data.backup.LauncherBackupData
import de.fisch981.minimallauncher.data.model.HomeGestureAction
import de.fisch981.minimallauncher.data.model.ScrollerSide
import de.fisch981.minimallauncher.ui.theme.AppLanguage
import de.fisch981.minimallauncher.ui.theme.LauncherFont
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "minimal_launcher_prefs")

class FavoritesRepository(private val context: Context) {

    companion object {
        private val KEY_FAVORITES = stringPreferencesKey("favorites_order")
        private val KEY_HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        private val KEY_POPUP_FOLDERS = stringPreferencesKey("popup_folders_json")
        private val KEY_RIGHT_HANDED = booleanPreferencesKey("right_handed_scroller")
        private val KEY_SCROLLER_SIDE = stringPreferencesKey("scroller_side")
        private val KEY_SHOW_MEDIA = booleanPreferencesKey("show_media_widget")
        private val KEY_SHOW_BATTERY = booleanPreferencesKey("show_battery")
        private val KEY_SHOW_NEXT_ALARM = booleanPreferencesKey("show_next_alarm")
        private val KEY_CUSTOM_ICONS = stringPreferencesKey("custom_icons_overrides")
        private val KEY_CUSTOM_NAMES = stringPreferencesKey("custom_app_names")
        private val KEY_RECENT_APPS = stringPreferencesKey("recent_apps_order")
        private val KEY_SHOW_RECENT_APPS = booleanPreferencesKey("show_recent_apps")
        private val KEY_LEFT_WAVE_COUNT = intPreferencesKey("left_wave_usage_count")
        private val KEY_RIGHT_WAVE_COUNT = intPreferencesKey("right_wave_usage_count")
        private val KEY_APP_LAUNCH_COUNTS = stringPreferencesKey("app_launch_counts")
        private val KEY_WAVE_IDLE_ALPHA = intPreferencesKey("wave_idle_alpha_percent")
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_SHOW_CLOCK = booleanPreferencesKey("show_clock")
        private val KEY_CLOCK_SIZE_SP = intPreferencesKey("clock_size_sp")
        private val KEY_SHOW_DATE = booleanPreferencesKey("show_date")
        private val KEY_SHOW_APP_ICONS = booleanPreferencesKey("show_app_icons")
        private val KEY_HIDE_STATUS_BAR = booleanPreferencesKey("hide_status_bar")
        private val KEY_SHOW_BOTTOM_ACTIONS = booleanPreferencesKey("show_bottom_actions")
        private val KEY_SHOW_NOTIFICATION_APPS = booleanPreferencesKey("show_notification_apps")
        private val KEY_SHOW_NOTIFICATION_PREVIEW = booleanPreferencesKey("show_notification_preview")
        private val KEY_SHOW_NOTIFICATION_COUNT = booleanPreferencesKey("show_notification_count")
        private val KEY_HOME_SWIPE_DOWN_ACTION = stringPreferencesKey("home_swipe_down_action")
        private val KEY_HOME_SWIPE_UP_ACTION = stringPreferencesKey("home_swipe_up_action")
        private val KEY_HOME_DOUBLE_TAP_ACTION = stringPreferencesKey("home_double_tap_action")
        private val KEY_APP_FONT = stringPreferencesKey("app_font")
        private val KEY_CLOCK_DATE_FONT = stringPreferencesKey("clock_date_font")
        private val KEY_WAVE_FONT = stringPreferencesKey("wave_font")
        private val KEY_APP_FONT_SIZE_SP = intPreferencesKey("app_font_size_sp")
        private val KEY_APP_ROW_SPACING_DP = intPreferencesKey("app_row_spacing_dp")
        private val KEY_DATE_SIZE_SP = intPreferencesKey("date_size_sp")
        private val KEY_WAVE_FONT_SIZE_SP = intPreferencesKey("wave_font_size_sp")
        private val KEY_APP_LONG_PRESS_DELAY_MS = intPreferencesKey("app_long_press_delay_ms")
    }

    val customIconsFlow: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_CUSTOM_ICONS] ?: ""
        parseCustomIcons(raw)
    }

    val customNamesFlow: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        parseCustomIcons(prefs[KEY_CUSTOM_NAMES] ?: "")
    }

    // Comma-separated list of "packageName/activityName"
    val favoritesFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_FAVORITES] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
    }

    val hiddenAppsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_HIDDEN_APPS] ?: emptySet()
    }

    val rightHandedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_RIGHT_HANDED] ?: true
    }

    val scrollerSideFlow: Flow<ScrollerSide> = context.dataStore.data.map { prefs ->
        ScrollerSide.fromStored(prefs[KEY_SCROLLER_SIDE], prefs[KEY_RIGHT_HANDED] ?: true)
    }

    val showMediaFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_MEDIA] ?: true
    }

    val showBatteryFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_BATTERY] ?: true
    }

    val showNextAlarmFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_NEXT_ALARM] ?: true
    }

    val showClockFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_CLOCK] ?: true
    }

    val clockSizeSpFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        (prefs[KEY_CLOCK_SIZE_SP] ?: 52).coerceIn(28, 84)
    }

    val showDateFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_DATE] ?: true
    }

    val showAppIconsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_APP_ICONS] ?: true
    }

    val hideStatusBarFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HIDE_STATUS_BAR] ?: false
    }

    val showBottomActionsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_BOTTOM_ACTIONS] ?: true
    }

    val showNotificationAppsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_NOTIFICATION_APPS] ?: true
    }

    val showNotificationPreviewFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_NOTIFICATION_PREVIEW] ?: true
    }

    val showNotificationCountFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_NOTIFICATION_COUNT] ?: true
    }

    val homeSwipeDownActionFlow: Flow<HomeGestureAction> = context.dataStore.data.map { prefs ->
        HomeGestureAction.fromCode(
            prefs[KEY_HOME_SWIPE_DOWN_ACTION],
            HomeGestureAction.EXPAND_NOTIFICATIONS
        )
    }

    val homeSwipeUpActionFlow: Flow<HomeGestureAction> = context.dataStore.data.map { prefs ->
        HomeGestureAction.fromCode(
            prefs[KEY_HOME_SWIPE_UP_ACTION],
            HomeGestureAction.SEARCH
        )
    }

    val homeDoubleTapActionFlow: Flow<HomeGestureAction> = context.dataStore.data.map { prefs ->
        HomeGestureAction.fromCode(
            prefs[KEY_HOME_DOUBLE_TAP_ACTION],
            HomeGestureAction.NONE
        )
    }

    val appFontFlow: Flow<LauncherFont> = context.dataStore.data.map { prefs ->
        LauncherFont.fromCode(prefs[KEY_APP_FONT])
    }

    val clockDateFontFlow: Flow<LauncherFont> = context.dataStore.data.map { prefs ->
        LauncherFont.fromCode(prefs[KEY_CLOCK_DATE_FONT])
    }

    val waveFontFlow: Flow<LauncherFont> = context.dataStore.data.map { prefs ->
        LauncherFont.fromCode(prefs[KEY_WAVE_FONT])
    }

    val appFontSizeSpFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        (prefs[KEY_APP_FONT_SIZE_SP] ?: 17).coerceIn(13, 24)
    }

    val appRowSpacingDpFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        (prefs[KEY_APP_ROW_SPACING_DP] ?: 8).coerceIn(2, 18)
    }

    val dateSizeSpFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        (prefs[KEY_DATE_SIZE_SP] ?: 15).coerceIn(11, 24)
    }

    val waveFontSizeSpFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        (prefs[KEY_WAVE_FONT_SIZE_SP] ?: 11).coerceIn(9, 16)
    }

    val appLongPressDelayMsFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        (prefs[KEY_APP_LONG_PRESS_DELAY_MS] ?: 500).coerceIn(250, 2000)
    }

    val recentAppsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_RECENT_APPS] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
    }

    val showRecentAppsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_RECENT_APPS] ?: true
    }

    val waveUsageStatsFlow: Flow<WaveUsageStats> = context.dataStore.data.map { prefs ->
        WaveUsageStats(
            leftCount = prefs[KEY_LEFT_WAVE_COUNT] ?: 0,
            rightCount = prefs[KEY_RIGHT_WAVE_COUNT] ?: 0
        )
    }

    val waveIdleAlphaPercentFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        (prefs[KEY_WAVE_IDLE_ALPHA] ?: 40).coerceIn(0, 100)
    }

    val appLanguageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        AppLanguage.fromCode(prefs[KEY_APP_LANGUAGE])
    }

    val appLaunchCountsFlow: Flow<Map<String, Int>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_APP_LAUNCH_COUNTS] ?: ""
        parseCustomIcons(raw).mapNotNull { (k, v) ->
            v.toIntOrNull()?.takeIf { it > 0 }?.let { k to it }
        }.toMap()
    }

    // Folders map: "parentKey:child1,child2;parentKey2:child3,child4"
    val popupFoldersFlow: Flow<Map<String, List<String>>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_POPUP_FOLDERS] ?: ""
        parseFolders(raw)
    }

    suspend fun addFavorite(appKey: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_FAVORITES] ?: "").split(",").filter { it.isNotBlank() }.toMutableList()
            if (!current.contains(appKey)) {
                current.add(appKey)
                prefs[KEY_FAVORITES] = current.joinToString(",")
            }
        }
    }

    suspend fun removeFavorite(appKey: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_FAVORITES] ?: "").split(",").filter { it.isNotBlank() }.toMutableList()
            current.remove(appKey)
            prefs[KEY_FAVORITES] = current.joinToString(",")
        }
    }

    suspend fun toggleFavorite(appKey: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_FAVORITES] ?: "").split(",").filter { it.isNotBlank() }.toMutableList()
            if (current.contains(appKey)) {
                current.remove(appKey)
            } else {
                current.add(appKey)
            }
            prefs[KEY_FAVORITES] = current.joinToString(",")
        }
    }

    suspend fun setFavoritesOrder(newOrder: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FAVORITES] = newOrder.joinToString(",")
        }
    }

    suspend fun hideApp(appKey: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_HIDDEN_APPS] ?: emptySet()).toMutableSet()
            current.add(appKey)
            prefs[KEY_HIDDEN_APPS] = current
        }
    }

    suspend fun unhideApp(appKey: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_HIDDEN_APPS] ?: emptySet()).toMutableSet()
            current.remove(appKey)
            prefs[KEY_HIDDEN_APPS] = current
        }
    }

    suspend fun addSubAppToFolder(parentKey: String, childKey: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[KEY_POPUP_FOLDERS] ?: ""
            val map = parseFolders(raw).toMutableMap()
            val list = map[parentKey]?.toMutableList() ?: mutableListOf()
            if (!list.contains(childKey)) {
                list.add(childKey)
                map[parentKey] = list
                prefs[KEY_POPUP_FOLDERS] = serializeFolders(map)
            }
        }
    }

    suspend fun removeSubAppFromFolder(parentKey: String, childKey: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[KEY_POPUP_FOLDERS] ?: ""
            val map = parseFolders(raw).toMutableMap()
            val list = map[parentKey]?.toMutableList() ?: return@edit
            list.remove(childKey)
            if (list.isEmpty()) {
                map.remove(parentKey)
            } else {
                map[parentKey] = list
            }
            prefs[KEY_POPUP_FOLDERS] = serializeFolders(map)
        }
    }

    suspend fun setRightHanded(rightHanded: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RIGHT_HANDED] = rightHanded
            prefs[KEY_SCROLLER_SIDE] = if (rightHanded) ScrollerSide.RIGHT.name else ScrollerSide.LEFT.name
        }
    }

    suspend fun setScrollerSide(side: ScrollerSide) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCROLLER_SIDE] = side.name
            prefs[KEY_RIGHT_HANDED] = (side != ScrollerSide.LEFT)
        }
    }

    suspend fun setShowMedia(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_MEDIA] = show
        }
    }

    suspend fun setShowBattery(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_BATTERY] = show
        }
    }

    suspend fun setShowNextAlarm(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_NEXT_ALARM] = show
        }
    }

    suspend fun setShowClock(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_CLOCK] = show
        }
    }

    suspend fun setClockSizeSp(sizeSp: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CLOCK_SIZE_SP] = sizeSp.coerceIn(28, 84)
        }
    }

    suspend fun setShowDate(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_DATE] = show
        }
    }

    suspend fun setShowAppIcons(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_APP_ICONS] = show
        }
    }

    suspend fun setHideStatusBar(hide: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HIDE_STATUS_BAR] = hide
        }
    }

    suspend fun setShowBottomActions(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_BOTTOM_ACTIONS] = show
        }
    }

    suspend fun setShowNotificationApps(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_NOTIFICATION_APPS] = show
        }
    }

    suspend fun setShowNotificationPreview(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_NOTIFICATION_PREVIEW] = show
        }
    }

    suspend fun setShowNotificationCount(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_NOTIFICATION_COUNT] = show
        }
    }

    suspend fun setHomeSwipeDownAction(action: HomeGestureAction) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HOME_SWIPE_DOWN_ACTION] = action.code
        }
    }

    suspend fun setHomeSwipeUpAction(action: HomeGestureAction) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HOME_SWIPE_UP_ACTION] = action.code
        }
    }

    suspend fun setHomeDoubleTapAction(action: HomeGestureAction) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HOME_DOUBLE_TAP_ACTION] = action.code
        }
    }

    suspend fun setAppFont(font: LauncherFont) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_FONT] = font.code
        }
    }

    suspend fun setClockDateFont(font: LauncherFont) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CLOCK_DATE_FONT] = font.code
        }
    }

    suspend fun setWaveFont(font: LauncherFont) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WAVE_FONT] = font.code
        }
    }

    suspend fun setAppFontSizeSp(sizeSp: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_FONT_SIZE_SP] = sizeSp.coerceIn(13, 24)
        }
    }

    suspend fun setAppRowSpacingDp(spacingDp: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_ROW_SPACING_DP] = spacingDp.coerceIn(2, 18)
        }
    }

    suspend fun setDateSizeSp(sizeSp: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DATE_SIZE_SP] = sizeSp.coerceIn(11, 24)
        }
    }

    suspend fun setWaveFontSizeSp(sizeSp: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WAVE_FONT_SIZE_SP] = sizeSp.coerceIn(9, 16)
        }
    }

    suspend fun setAppLongPressDelayMs(delayMs: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_LONG_PRESS_DELAY_MS] = delayMs.coerceIn(250, 2000)
        }
    }

    suspend fun recordAppLaunch(appKey: String, isFavorite: Boolean = false) {
        if (appKey.isBlank()) return
        context.dataStore.edit { prefs ->
            val raw = prefs[KEY_RECENT_APPS] ?: ""
            val current = if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
            val updated = if (isFavorite) {
                current.filter { it != appKey }
            } else {
                (listOf(appKey) + current.filter { it != appKey }).take(15)
            }
            prefs[KEY_RECENT_APPS] = updated.joinToString(",")

            val countsRaw = prefs[KEY_APP_LAUNCH_COUNTS] ?: ""
            val counts = parseCustomIcons(countsRaw).mapNotNull { (k, v) ->
                v.toIntOrNull()?.takeIf { it > 0 }?.let { k to it }
            }.toMap().toMutableMap()
            counts[appKey] = (counts[appKey] ?: 0) + 1
            prefs[KEY_APP_LAUNCH_COUNTS] = serializeCustomIcons(counts.mapValues { it.value.toString() })
        }
    }

    suspend fun setShowRecentApps(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_RECENT_APPS] = show
        }
    }

    suspend fun recordWaveUsage(isRightSide: Boolean) {
        context.dataStore.edit { prefs ->
            val key = if (isRightSide) KEY_RIGHT_WAVE_COUNT else KEY_LEFT_WAVE_COUNT
            prefs[key] = (prefs[key] ?: 0) + 1
        }
    }

    suspend fun resetUsageStats() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_LEFT_WAVE_COUNT)
            prefs.remove(KEY_RIGHT_WAVE_COUNT)
            prefs.remove(KEY_APP_LAUNCH_COUNTS)
        }
    }

    suspend fun setWaveIdleAlphaPercent(percent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WAVE_IDLE_ALPHA] = percent.coerceIn(0, 100)
        }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_LANGUAGE] = language.code
        }
    }

    suspend fun saveCustomIcons(overrides: Map<String, String>) {
        context.dataStore.edit { prefs ->
            val current = parseCustomIcons(prefs[KEY_CUSTOM_ICONS] ?: "").toMutableMap()
            current.putAll(overrides)
            prefs[KEY_CUSTOM_ICONS] = serializeCustomIcons(current)
        }
    }

    suspend fun removeCustomIconForApp(vararg keys: String) {
        context.dataStore.edit { prefs ->
            val current = parseCustomIcons(prefs[KEY_CUSTOM_ICONS] ?: "").toMutableMap()
            var changed = false
            for (k in keys) {
                if (current.remove(k) != null) changed = true
            }
            if (changed) {
                prefs[KEY_CUSTOM_ICONS] = serializeCustomIcons(current)
            }
        }
    }

    suspend fun clearCustomIcons() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_CUSTOM_ICONS)
        }
    }

    suspend fun setCustomAppName(
        appKey: String,
        componentKey: String,
        packageName: String,
        customName: String
    ) {
        context.dataStore.edit { prefs ->
            val current = parseCustomIcons(prefs[KEY_CUSTOM_NAMES] ?: "").toMutableMap()
            val trimmed = customName.trim()
            if (trimmed.isBlank()) {
                current.remove(appKey)
                current.remove(componentKey)
                current.remove(packageName)
            } else {
                current[appKey] = trimmed
                current[componentKey] = trimmed
                current[packageName] = trimmed
            }
            prefs[KEY_CUSTOM_NAMES] = serializeCustomIcons(current)
        }
    }

    suspend fun saveImportedCustomNames(names: Map<String, String>) {
        context.dataStore.edit { prefs ->
            val current = parseCustomIcons(prefs[KEY_CUSTOM_NAMES] ?: "").toMutableMap()
            for ((k, v) in names) {
                if (!current.containsKey(k) && v.isNotBlank()) {
                    current[k] = v.trim()
                }
            }
            prefs[KEY_CUSTOM_NAMES] = serializeCustomIcons(current)
        }
    }

    private fun parseCustomIcons(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        val map = mutableMapOf<String, String>()
        val entries = raw.split("||")
        for (entry in entries) {
            val parts = entry.split("==")
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                map[parts[0]] = parts[1]
            }
        }
        return map
    }

    private fun serializeCustomIcons(map: Map<String, String>): String {
        return map.entries.joinToString("||") { "${it.key}==${it.value}" }
    }

    private fun parseFolders(raw: String): Map<String, List<String>> {
        if (raw.isBlank()) return emptyMap()
        val map = mutableMapOf<String, List<String>>()
        val entries = raw.split(";")
        for (entry in entries) {
            val parts = entry.split(":")
            if (parts.size == 2) {
                val parent = parts[0]
                val children = parts[1].split(",").filter { it.isNotBlank() }
                map[parent] = children
            }
        }
        return map
    }

    private fun serializeFolders(map: Map<String, List<String>>): String {
        return map.entries.joinToString(";") { "${it.key}:${it.value.joinToString(",")}" }
    }

    suspend fun exportBackupData(importedIconFiles: Map<String, String>): LauncherBackupData {
        val prefs = context.dataStore.data.first()
        val favsRaw = prefs[KEY_FAVORITES] ?: ""
        val recentsRaw = prefs[KEY_RECENT_APPS] ?: ""
        val launchCounts = parseCustomIcons(prefs[KEY_APP_LAUNCH_COUNTS] ?: "")
            .mapNotNull { (k, v) -> v.toIntOrNull()?.takeIf { it > 0 }?.let { k to it } }
            .toMap()
        val side = ScrollerSide.fromStored(prefs[KEY_SCROLLER_SIDE], prefs[KEY_RIGHT_HANDED] ?: true)

        return LauncherBackupData(
            favorites = if (favsRaw.isBlank()) emptyList() else favsRaw.split(",").filter { it.isNotBlank() },
            hiddenApps = prefs[KEY_HIDDEN_APPS] ?: emptySet(),
            popupFolders = parseFolders(prefs[KEY_POPUP_FOLDERS] ?: ""),
            scrollerSide = side.name,
            waveIdleAlphaPercent = (prefs[KEY_WAVE_IDLE_ALPHA] ?: 40).coerceIn(0, 100),
            appLanguage = AppLanguage.fromCode(prefs[KEY_APP_LANGUAGE]).code,
            appFont = LauncherFont.fromCode(prefs[KEY_APP_FONT]).code,
            clockDateFont = LauncherFont.fromCode(prefs[KEY_CLOCK_DATE_FONT]).code,
            waveFont = LauncherFont.fromCode(prefs[KEY_WAVE_FONT]).code,
            appFontSizeSp = (prefs[KEY_APP_FONT_SIZE_SP] ?: 17).coerceIn(13, 24),
            appRowSpacingDp = (prefs[KEY_APP_ROW_SPACING_DP] ?: 8).coerceIn(2, 18),
            dateSizeSp = (prefs[KEY_DATE_SIZE_SP] ?: 15).coerceIn(11, 24),
            waveFontSizeSp = (prefs[KEY_WAVE_FONT_SIZE_SP] ?: 11).coerceIn(9, 16),
            appLongPressDelayMs = (prefs[KEY_APP_LONG_PRESS_DELAY_MS] ?: 500).coerceIn(250, 2000),
            showClock = prefs[KEY_SHOW_CLOCK] ?: true,
            clockSizeSp = (prefs[KEY_CLOCK_SIZE_SP] ?: 52).coerceIn(28, 84),
            showDate = prefs[KEY_SHOW_DATE] ?: true,
            showMedia = prefs[KEY_SHOW_MEDIA] ?: true,
            showBattery = prefs[KEY_SHOW_BATTERY] ?: true,
            showNextAlarm = prefs[KEY_SHOW_NEXT_ALARM] ?: true,
            showRecentApps = prefs[KEY_SHOW_RECENT_APPS] ?: true,
            showAppIcons = prefs[KEY_SHOW_APP_ICONS] ?: true,
            hideStatusBar = prefs[KEY_HIDE_STATUS_BAR] ?: false,
            showBottomActions = prefs[KEY_SHOW_BOTTOM_ACTIONS] ?: true,
            showNotificationApps = prefs[KEY_SHOW_NOTIFICATION_APPS] ?: true,
            showNotificationPreview = prefs[KEY_SHOW_NOTIFICATION_PREVIEW] ?: true,
            showNotificationCount = prefs[KEY_SHOW_NOTIFICATION_COUNT] ?: true,
            homeSwipeDownAction = HomeGestureAction.fromCode(
                prefs[KEY_HOME_SWIPE_DOWN_ACTION],
                HomeGestureAction.EXPAND_NOTIFICATIONS
            ).code,
            homeSwipeUpAction = HomeGestureAction.fromCode(
                prefs[KEY_HOME_SWIPE_UP_ACTION],
                HomeGestureAction.SEARCH
            ).code,
            homeDoubleTapAction = HomeGestureAction.fromCode(
                prefs[KEY_HOME_DOUBLE_TAP_ACTION],
                HomeGestureAction.NONE
            ).code,
            recentApps = if (recentsRaw.isBlank()) emptyList() else recentsRaw.split(",").filter { it.isNotBlank() },
            leftWaveCount = (prefs[KEY_LEFT_WAVE_COUNT] ?: 0).coerceAtLeast(0),
            rightWaveCount = (prefs[KEY_RIGHT_WAVE_COUNT] ?: 0).coerceAtLeast(0),
            appLaunchCounts = launchCounts,
            customIcons = parseCustomIcons(prefs[KEY_CUSTOM_ICONS] ?: ""),
            customNames = parseCustomIcons(prefs[KEY_CUSTOM_NAMES] ?: ""),
            importedIconFiles = importedIconFiles
        )
    }

    suspend fun restoreBackupData(data: LauncherBackupData) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FAVORITES] = data.favorites.filter { it.isNotBlank() }.joinToString(",")
            prefs[KEY_HIDDEN_APPS] = data.hiddenApps.filter { it.isNotBlank() }.toSet()
            prefs[KEY_POPUP_FOLDERS] = serializeFolders(data.popupFolders.filter { it.key.isNotBlank() && it.value.isNotEmpty() })

            val side = ScrollerSide.fromStored(data.scrollerSide, true)
            prefs[KEY_SCROLLER_SIDE] = side.name
            prefs[KEY_RIGHT_HANDED] = (side != ScrollerSide.LEFT)

            prefs[KEY_WAVE_IDLE_ALPHA] = data.waveIdleAlphaPercent.coerceIn(0, 100)
            prefs[KEY_APP_LANGUAGE] = AppLanguage.fromCode(data.appLanguage).code
            prefs[KEY_APP_FONT] = LauncherFont.fromCode(data.appFont).code
            prefs[KEY_CLOCK_DATE_FONT] = LauncherFont.fromCode(data.clockDateFont).code
            prefs[KEY_WAVE_FONT] = LauncherFont.fromCode(data.waveFont).code
            prefs[KEY_APP_FONT_SIZE_SP] = data.appFontSizeSp.coerceIn(13, 24)
            prefs[KEY_APP_ROW_SPACING_DP] = data.appRowSpacingDp.coerceIn(2, 18)
            prefs[KEY_DATE_SIZE_SP] = data.dateSizeSp.coerceIn(11, 24)
            prefs[KEY_WAVE_FONT_SIZE_SP] = data.waveFontSizeSp.coerceIn(9, 16)
            prefs[KEY_APP_LONG_PRESS_DELAY_MS] = data.appLongPressDelayMs.coerceIn(250, 2000)
            prefs[KEY_SHOW_CLOCK] = data.showClock
            prefs[KEY_CLOCK_SIZE_SP] = data.clockSizeSp.coerceIn(28, 84)
            prefs[KEY_SHOW_DATE] = data.showDate
            prefs[KEY_SHOW_MEDIA] = data.showMedia
            prefs[KEY_SHOW_BATTERY] = data.showBattery
            prefs[KEY_SHOW_NEXT_ALARM] = data.showNextAlarm
            prefs[KEY_SHOW_RECENT_APPS] = data.showRecentApps
            prefs[KEY_SHOW_APP_ICONS] = data.showAppIcons
            prefs[KEY_HIDE_STATUS_BAR] = data.hideStatusBar
            prefs[KEY_SHOW_BOTTOM_ACTIONS] = data.showBottomActions
            prefs[KEY_SHOW_NOTIFICATION_APPS] = data.showNotificationApps
            prefs[KEY_SHOW_NOTIFICATION_PREVIEW] = data.showNotificationPreview
            prefs[KEY_SHOW_NOTIFICATION_COUNT] = data.showNotificationCount
            prefs[KEY_HOME_SWIPE_DOWN_ACTION] = HomeGestureAction.fromCode(
                data.homeSwipeDownAction,
                HomeGestureAction.EXPAND_NOTIFICATIONS
            ).code
            prefs[KEY_HOME_SWIPE_UP_ACTION] = HomeGestureAction.fromCode(
                data.homeSwipeUpAction,
                HomeGestureAction.SEARCH
            ).code
            prefs[KEY_HOME_DOUBLE_TAP_ACTION] = HomeGestureAction.fromCode(
                data.homeDoubleTapAction,
                HomeGestureAction.NONE
            ).code
            prefs[KEY_RECENT_APPS] = data.recentApps.filter { it.isNotBlank() }.take(15).joinToString(",")
            prefs[KEY_LEFT_WAVE_COUNT] = data.leftWaveCount.coerceAtLeast(0)
            prefs[KEY_RIGHT_WAVE_COUNT] = data.rightWaveCount.coerceAtLeast(0)
            prefs[KEY_APP_LAUNCH_COUNTS] = serializeCustomIcons(
                data.appLaunchCounts.filter { it.key.isNotBlank() && it.value > 0 }.mapValues { it.value.toString() }
            )
            prefs[KEY_CUSTOM_ICONS] = serializeCustomIcons(data.customIcons.filter { it.key.isNotBlank() && it.value.isNotBlank() })
            prefs[KEY_CUSTOM_NAMES] = serializeCustomIcons(data.customNames.filter { it.key.isNotBlank() && it.value.isNotBlank() })
        }
    }
}

data class WaveUsageStats(
    val leftCount: Int = 0,
    val rightCount: Int = 0
) {
    val totalCount: Int get() = leftCount + rightCount
    val leftPercent: Int get() = if (totalCount > 0) ((leftCount * 100f) / totalCount).toInt() else 0
    val rightPercent: Int get() = if (totalCount > 0) 100 - leftPercent else 0
}
