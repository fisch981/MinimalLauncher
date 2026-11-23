package de.fisch981.minimallauncher.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.fisch981.minimallauncher.data.model.AppItem
import de.fisch981.minimallauncher.data.model.AppNotificationItem
import de.fisch981.minimallauncher.data.model.MediaState
import de.fisch981.minimallauncher.data.model.ScrollerSide
import de.fisch981.minimallauncher.data.model.ShortcutItem
import de.fisch981.minimallauncher.data.nova.ImportedIconStore
import de.fisch981.minimallauncher.data.nova.NovaBackupImporter
import de.fisch981.minimallauncher.data.repository.AppRepository
import de.fisch981.minimallauncher.data.repository.FavoritesRepository
import de.fisch981.minimallauncher.data.repository.IconPackManager
import de.fisch981.minimallauncher.data.repository.LauncherUtils
import de.fisch981.minimallauncher.data.repository.StartupCache
import de.fisch981.minimallauncher.data.repository.WaveUsageStats
import de.fisch981.minimallauncher.data.service.LauncherNotificationService
import de.fisch981.minimallauncher.data.service.MediaSessionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    val importedIcons = ImportedIconStore(application)
    val iconPackManager = IconPackManager(application, importedIcons)
    val appRepository = AppRepository(application, iconPackManager)
    val favoritesRepository = FavoritesRepository(application)
    val mediaHelper = MediaSessionHelper(application)
    val novaBackupImporter = NovaBackupImporter(application, importedIcons)
    private val startupCache = StartupCache(application)
    private val initialCachedState = startupCache.loadInitialUiState()

    private val _selectedAppForPopup = MutableStateFlow<AppItem?>(null)
    val selectedAppForPopup: StateFlow<AppItem?> = _selectedAppForPopup.asStateFlow()

    private val _selectedAppShortcuts = MutableStateFlow<List<ShortcutItem>>(emptyList())
    val selectedAppShortcuts: StateFlow<List<ShortcutItem>> = _selectedAppShortcuts.asStateFlow()

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isManageFavoritesOpen = MutableStateFlow(false)
    val isManageFavoritesOpen: StateFlow<Boolean> = _isManageFavoritesOpen.asStateFlow()

    private val _appForIconEditing = MutableStateFlow<AppItem?>(null)
    val appForIconEditing: StateFlow<AppItem?> = _appForIconEditing.asStateFlow()

    private val _appForFolderEditing = MutableStateFlow<AppItem?>(null)
    val appForFolderEditing: StateFlow<AppItem?> = _appForFolderEditing.asStateFlow()

    private val _appForRenaming = MutableStateFlow<AppItem?>(null)
    val appForRenaming: StateFlow<AppItem?> = _appForRenaming.asStateFlow()

    val isRightHanded: StateFlow<Boolean> = favoritesRepository.rightHandedFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val scrollerSide: StateFlow<ScrollerSide> = favoritesRepository.scrollerSideFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ScrollerSide.RIGHT)

    val showBattery: StateFlow<Boolean> = favoritesRepository.showBatteryFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val showMedia: StateFlow<Boolean> = favoritesRepository.showMediaFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val showRecentApps: StateFlow<Boolean> = favoritesRepository.showRecentAppsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val waveUsageStats: StateFlow<WaveUsageStats> = favoritesRepository.waveUsageStatsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, WaveUsageStats())

    val waveIdleAlphaPercent: StateFlow<Int> = favoritesRepository.waveIdleAlphaPercentFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 40)

    private val _scrollToTopEvent = MutableStateFlow(0)
    val scrollToTopEvent: StateFlow<Int> = _scrollToTopEvent.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    fun requestScrollToTop() {
        _scrollToTopEvent.value = _scrollToTopEvent.value + 1
    }

    fun onReturnToLauncher(closeSettings: Boolean = false) {
        closePopup()
        _isSearchOpen.value = false
        if (closeSettings) {
            _isSettingsOpen.value = false
            _isManageFavoritesOpen.value = false
            _appForIconEditing.value = null
            _appForFolderEditing.value = null
            _appForRenaming.value = null
        }
        requestScrollToTop()
    }

    fun checkDefaultLauncher() {
        _isDefaultLauncher.value = LauncherUtils.isDefaultLauncher(getApplication())
    }

    val notifications: StateFlow<Map<String, List<AppNotificationItem>>> =
        LauncherNotificationService.notifications

    val mediaState: StateFlow<MediaState> = mediaHelper.mediaState

    // Combined state for favorites and all apps with folders mapped
    val launcherState = combine(
        appRepository.installedApps,
        combine(
            favoritesRepository.favoritesFlow,
            favoritesRepository.recentAppsFlow,
            favoritesRepository.appLaunchCountsFlow,
            ::Triple
        ),
        favoritesRepository.popupFoldersFlow,
        favoritesRepository.hiddenAppsFlow,
        favoritesRepository.customNamesFlow
    ) { allApps, (favoriteKeys, recentKeys, launchCounts), foldersMap, hiddenKeys, customNames ->

        val renamedByKey = allApps.associate { app ->
            val componentKey = "${app.packageName}/${app.activityName}"
            val customName = customNames[app.key] ?: customNames[componentKey] ?: customNames[app.packageName]
            val effectiveLabel = if (!customName.isNullOrBlank()) customName else app.label
            app.key to app.copy(label = effectiveLabel, originalLabel = app.label)
        }

        val processedApps = allApps
            .filter { !hiddenKeys.contains(it.key) }
            .mapNotNull { rawApp ->
                val app = renamedByKey[rawApp.key] ?: return@mapNotNull null
                val subAppKeys = foldersMap[app.key] ?: emptyList()
                val subApps = subAppKeys.mapNotNull { renamedByKey[it] }
                val isFav = favoriteKeys.contains(app.key)
                app.copy(
                    isFavorite = isFav,
                    subApps = subApps,
                    isHidden = false
                )
            }

        val matchedFavorites = favoriteKeys.mapNotNull { key ->
            processedApps.firstOrNull { app ->
                app.key == key ||
                    "${app.packageName}/${app.activityName}" == key ||
                    app.packageName == key
            }
        }
            .distinctBy { it.key }
            .sortedBy { it.label.lowercase() }

        val favoriteApps = matchedFavorites

        val favoriteKeySet = favoriteApps.map { it.key }.toSet()
        val finalProcessedApps = processedApps.map { app ->
            if (app.isFavorite != favoriteKeySet.contains(app.key)) {
                app.copy(isFavorite = favoriteKeySet.contains(app.key))
            } else {
                app
            }
        }

        val recentApps = recentKeys.mapNotNull { key ->
            finalProcessedApps.firstOrNull { app ->
                app.key == key ||
                    "${app.packageName}/${app.activityName}" == key ||
                    app.packageName == key
            }
        }
            .filter { !it.isFavorite }
            .distinctBy { it.key }
            .take(5)

        val mostUsedApps = finalProcessedApps.mapNotNull { app ->
            val count = launchCounts[app.key]
                ?: launchCounts["${app.packageName}/${app.activityName}"]
                ?: launchCounts[app.packageName]
                ?: 0
            if (count > 0) AppLaunchStat(app = app, launchCount = count) else null
        }.sortedWith(
            compareByDescending<AppLaunchStat> { it.launchCount }
                .thenBy { it.app.label.lowercase() }
        ).take(10)

        val alphabeticalApps = finalProcessedApps.sortedBy { it.label.lowercase() }
        val availableLetters = if (allApps === initialCachedState.favoriteApps && initialCachedState.availableLetters.isNotEmpty()) {
            initialCachedState.availableLetters
        } else {
            alphabeticalApps.map { it.initialChar }.toSet()
        }
        val effectiveFavorites = if (favoriteApps.isEmpty() && allApps.isEmpty() && initialCachedState.favoriteApps.isNotEmpty()) {
            initialCachedState.favoriteApps
        } else {
            favoriteApps
        }

        LauncherUiState(
            favoriteApps = effectiveFavorites,
            recentApps = recentApps,
            mostUsedApps = mostUsedApps,
            allApps = alphabeticalApps,
            availableLetters = availableLetters,
            hiddenApps = allApps.filter { hiddenKeys.contains(it.key) }.mapNotNull { renamedByKey[it.key] }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialCachedState
    )

    private val _customIconsCount = MutableStateFlow(0)
    val customIconsCountState: StateFlow<Int> = _customIconsCount.asStateFlow()

    init {
        _customIconsCount.value = importedIcons.count()
        appRepository.seedInitialApps(initialCachedState.favoriteApps)
        appRepository.registerCallbackOnly()
        mediaHelper.startListening()

        viewModelScope.launch(Dispatchers.IO) {
            val initialOverrides = favoritesRepository.customIconsFlow.first()
            iconPackManager.setCustomOverrides(initialOverrides)
            iconPackManager.ensureInitialized()

            val favKeys = initialCachedState.favoriteApps.map { it.key }.toSet()
                .ifEmpty { favoritesRepository.favoritesFlow.first().toSet() }
            appRepository.refreshApps(priorityKeys = favKeys)

            // Manuell im Icon-Picker gewählte Lines-Icons bei Änderungen nachladen
            favoritesRepository.customIconsFlow.drop(1).collectLatest { overrides ->
                iconPackManager.setCustomOverrides(overrides)
                appRepository.refreshApps()
            }
        }

        // Aktuelle Favoriten samt kleinen Monochrom-Icons als Startup-Snapshot sichern (oder bei 0 Favoriten leeren)
        viewModelScope.launch {
            launcherState.collectLatest { state ->
                if (state.allApps.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        startupCache.saveSnapshot(state)
                    }
                }
            }
        }

        checkDefaultLauncher()
    }

    /** Importiert die Icon-Zuordnung (und Dock-/Home-Favoriten) aus einer .novabackup-Datei. */
    fun importNovaBackup(uri: Uri, onComplete: (count: Int, error: String?) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { novaBackupImporter.importFromUri(uri) }
            if (result.importedCount > 0) {
                if (!result.iconPackPackage.isNullOrBlank()) {
                    iconPackManager.loadIconPack(result.iconPackPackage)
                }
                if (result.favoriteComponents.isNotEmpty()) {
                    val installed = appRepository.installedApps.value
                    val matchedFavKeys = result.favoriteComponents.mapNotNull { comp ->
                        val pkg = comp.substringBefore('/')
                        installed.firstOrNull { app ->
                            "${app.packageName}/${app.activityName}" == comp ||
                                app.packageName == pkg
                        }?.key
                    }.distinct().take(10)
                    if (matchedFavKeys.isNotEmpty()) {
                        favoritesRepository.setFavoritesOrder(matchedFavKeys)
                    }
                }
                if (result.customLabels.isNotEmpty()) {
                    favoritesRepository.saveImportedCustomNames(result.customLabels)
                }
                _customIconsCount.value = importedIcons.count()
                iconPackManager.clearIconCache()
                appRepository.refreshApps()
            }
            onComplete(result.importedCount, result.errorMessage)
        }
    }

    fun clearCustomIcons() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { importedIcons.clear() }
            favoritesRepository.clearCustomIcons()
            _customIconsCount.value = 0
            iconPackManager.clearIconCache()
            appRepository.refreshApps()
        }
    }

    override fun onCleared() {
        super.onCleared()
        appRepository.stopListening()
        mediaHelper.stopListening()
    }

    fun launchApp(app: AppItem) {
        val isFav = app.isFavorite || launcherState.value.favoriteApps.any { it.key == app.key }
        viewModelScope.launch {
            favoritesRepository.recordAppLaunch(app.key, isFavorite = isFav)
        }
        onReturnToLauncher()
        appRepository.launchApp(app)
    }

    fun launchShortcut(shortcut: ShortcutItem) {
        onReturnToLauncher()
        appRepository.launchShortcut(shortcut)
    }

    fun openRenameDialog(app: AppItem) {
        _appForRenaming.value = app
    }

    fun closeRenameDialog() {
        _appForRenaming.value = null
    }

    fun renameApp(app: AppItem, newName: String) {
        viewModelScope.launch {
            favoritesRepository.setCustomAppName(
                appKey = app.key,
                componentKey = "${app.packageName}/${app.activityName}",
                packageName = app.packageName,
                customName = newName
            )
            closeRenameDialog()
        }
    }

    fun openPopup(app: AppItem) {
        _selectedAppForPopup.value = app
        viewModelScope.launch {
            _selectedAppShortcuts.value = appRepository.getAppShortcuts(app)
        }
    }

    fun closePopup() {
        _selectedAppForPopup.value = null
        _selectedAppShortcuts.value = emptyList()
    }

    fun toggleFavorite(app: AppItem) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(app.key)
        }
    }

    fun hideApp(app: AppItem) {
        viewModelScope.launch {
            favoritesRepository.hideApp(app.key)
        }
    }

    fun unhideApp(app: AppItem) {
        viewModelScope.launch {
            favoritesRepository.unhideApp(app.key)
        }
    }

    fun addSubApp(parent: AppItem, child: AppItem) {
        viewModelScope.launch {
            favoritesRepository.addSubAppToFolder(parent.key, child.key)
        }
    }

    fun openFolderEditor(app: AppItem) {
        _appForFolderEditing.value = app
    }

    fun closeFolderEditor() {
        _appForFolderEditing.value = null
    }

    fun toggleSubApp(parent: AppItem, child: AppItem) {
        viewModelScope.launch {
            val alreadyInFolder = parent.subApps.any { it.key == child.key }
            if (alreadyInFolder) {
                favoritesRepository.removeSubAppFromFolder(parent.key, child.key)
            } else {
                favoritesRepository.addSubAppToFolder(parent.key, child.key)
            }
        }
    }

    fun openIconEditor(app: AppItem) {
        _appForIconEditing.value = app
    }

    fun closeIconEditor() {
        _appForIconEditing.value = null
    }

    fun setCustomAppIcon(app: AppItem, drawableName: String) {
        viewModelScope.launch {
            favoritesRepository.saveCustomIcons(
                mapOf(
                    app.key to drawableName,
                    app.packageName to drawableName,
                    "${app.packageName}/${app.activityName}" to drawableName
                )
            )
            iconPackManager.clearIconCache()
            closeIconEditor()
        }
    }

    fun applyPickedIconFromResult(app: AppItem, data: Intent?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                val bitmap = iconPackManager.extractBitmapFromPickerResult(data)
                if (bitmap != null) {
                    importedIcons.saveBitmapForApp("${app.packageName}/${app.activityName}", app.packageName, bitmap)
                } else {
                    false
                }
            }
            if (saved) {
                favoritesRepository.removeCustomIconForApp(
                    app.key,
                    app.packageName,
                    "${app.packageName}/${app.activityName}"
                )
                _customIconsCount.value = importedIcons.count()
                iconPackManager.clearIconCache()
                appRepository.refreshApps()
            }
            onResult(saved)
        }
    }

    fun resetAppIcon(app: AppItem) {
        viewModelScope.launch {
            favoritesRepository.removeCustomIconForApp(
                app.key,
                app.packageName,
                "${app.packageName}/${app.activityName}"
            )
            withContext(Dispatchers.IO) {
                importedIcons.removeForApp("${app.packageName}/${app.activityName}", app.packageName)
            }
            _customIconsCount.value = importedIcons.count()
            iconPackManager.clearIconCache()
            appRepository.refreshApps()
            closeIconEditor()
        }
    }

    fun setSearchOpen(open: Boolean) {
        _isSearchOpen.value = open
    }

    fun setSettingsOpen(open: Boolean) {
        _isSettingsOpen.value = open
    }

    fun setManageFavoritesOpen(open: Boolean) {
        _isManageFavoritesOpen.value = open
    }

    fun toggleShowBattery(show: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setShowBattery(show)
        }
    }

    fun toggleShowRecentApps(show: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setShowRecentApps(show)
        }
    }

    fun recordWaveUsage(isRightSide: Boolean) {
        viewModelScope.launch {
            favoritesRepository.recordWaveUsage(isRightSide)
        }
    }

    fun resetUsageStats() {
        viewModelScope.launch {
            favoritesRepository.resetUsageStats()
        }
    }

    fun toggleShowMedia(show: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setShowMedia(show)
        }
    }

    fun toggleRightHanded(rightHanded: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setRightHanded(rightHanded)
        }
    }

    fun setScrollerSide(side: ScrollerSide) {
        viewModelScope.launch {
            favoritesRepository.setScrollerSide(side)
        }
    }

    fun setWaveIdleAlphaPercent(percent: Int) {
        viewModelScope.launch {
            favoritesRepository.setWaveIdleAlphaPercent(percent)
        }
    }

    fun reloadIcons() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                iconPackManager.clearIconCache()
                iconPackManager.initialize()
            }
            appRepository.refreshApps()
        }
    }

    fun mediaPlayPause() = mediaHelper.togglePlayPause()
    fun mediaNext() = mediaHelper.skipToNext()
    fun mediaPrevious() = mediaHelper.skipToPrevious()
}

data class AppLaunchStat(
    val app: AppItem,
    val launchCount: Int
)

data class LauncherUiState(
    val favoriteApps: List<AppItem> = emptyList(),
    val recentApps: List<AppItem> = emptyList(),
    val mostUsedApps: List<AppLaunchStat> = emptyList(),
    val allApps: List<AppItem> = emptyList(),
    val availableLetters: Set<Char> = emptySet(),
    val hiddenApps: List<AppItem> = emptyList()
)
