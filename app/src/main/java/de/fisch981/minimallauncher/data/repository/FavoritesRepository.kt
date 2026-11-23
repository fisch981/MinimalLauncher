package de.fisch981.minimallauncher.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.fisch981.minimallauncher.data.model.ScrollerSide
import kotlinx.coroutines.flow.Flow
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
        private val KEY_CUSTOM_ICONS = stringPreferencesKey("custom_icons_overrides")
        private val KEY_CUSTOM_NAMES = stringPreferencesKey("custom_app_names")
        private val KEY_RECENT_APPS = stringPreferencesKey("recent_apps_order")
        private val KEY_SHOW_RECENT_APPS = booleanPreferencesKey("show_recent_apps")
        private val KEY_LEFT_WAVE_COUNT = intPreferencesKey("left_wave_usage_count")
        private val KEY_RIGHT_WAVE_COUNT = intPreferencesKey("right_wave_usage_count")
        private val KEY_APP_LAUNCH_COUNTS = stringPreferencesKey("app_launch_counts")
        private val KEY_WAVE_IDLE_ALPHA = intPreferencesKey("wave_idle_alpha_percent")
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
}

data class WaveUsageStats(
    val leftCount: Int = 0,
    val rightCount: Int = 0
) {
    val totalCount: Int get() = leftCount + rightCount
    val leftPercent: Int get() = if (totalCount > 0) ((leftCount * 100f) / totalCount).toInt() else 0
    val rightPercent: Int get() = if (totalCount > 0) 100 - leftPercent else 0
}
