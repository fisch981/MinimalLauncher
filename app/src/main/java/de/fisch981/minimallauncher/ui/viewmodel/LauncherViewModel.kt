package de.fisch981.minimallauncher.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.fisch981.minimallauncher.data.backup.LauncherBackupCodec
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
import de.fisch981.minimallauncher.ui.theme.AppLanguage
import de.fisch981.minimallauncher.ui.theme.LauncherFont
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
import java.util.concurrent.ConcurrentHashMap

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

    private val _isHiddenAppsOpen = MutableStateFlow(false)
    val isHiddenAppsOpen: StateFlow<Boolean> = _isHiddenAppsOpen.asStateFlow()

    private val _isTypographyOpen = MutableStateFlow(false)
    val isTypographyOpen: StateFlow<Boolean> = _isTypographyOpen.asStateFlow()

    private val _isUsageStatsOpen = MutableStateFlow(false)
    val isUsageStatsOpen: StateFlow<Boolean> = _isUsageStatsOpen.asStateFlow()

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

    val showClock: StateFlow<Boolean> = favoritesRepository.showClockFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val clockSizeSp: StateFlow<Int> = favoritesRepository.clockSizeSpFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 52)

    val showDate: StateFlow<Boolean> = favoritesRepository.showDateFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val showMedia: StateFlow<Boolean> = favoritesRepository.showMediaFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val showRecentApps: StateFlow<Boolean> = favoritesRepository.showRecentAppsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val showAppIcons: StateFlow<Boolean> = favoritesRepository.showAppIconsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val hideStatusBar: StateFlow<Boolean> = favoritesRepository.hideStatusBarFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val showBottomActions: StateFlow<Boolean> = favoritesRepository.showBottomActionsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val showNotificationApps: StateFlow<Boolean> = favoritesRepository.showNotificationAppsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val showNotificationPreview: StateFlow<Boolean> = favoritesRepository.showNotificationPreviewFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val waveUsageStats: StateFlow<WaveUsageStats> = favoritesRepository.waveUsageStatsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, WaveUsageStats())

    val waveIdleAlphaPercent: StateFlow<Int> = favoritesRepository.waveIdleAlphaPercentFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 40)

    val appLanguage: StateFlow<AppLanguage> = favoritesRepository.appLanguageFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.SYSTEM)

    val appFont: StateFlow<LauncherFont> = favoritesRepository.appFontFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherFont.SYSTEM)

    val clockDateFont: StateFlow<LauncherFont> = favoritesRepository.clockDateFontFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherFont.SYSTEM)

    val waveFont: StateFlow<LauncherFont> = favoritesRepository.waveFontFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherFont.SYSTEM)

    val appFontSizeSp: StateFlow<Int> = favoritesRepository.appFontSizeSpFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 17)

    val appRowSpacingDp: StateFlow<Int> = favoritesRepository.appRowSpacingDpFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 8)

    val dateSizeSp: StateFlow<Int> = favoritesRepository.dateSizeSpFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 15)

    val waveFontSizeSp: StateFlow<Int> = favoritesRepository.waveFontSizeSpFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 11)

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
            _isHiddenAppsOpen.value = false
            _isTypographyOpen.value = false
            _isUsageStatsOpen.value = false
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

        fun isHiddenApp(app: AppItem): Boolean {
            val componentKey = "${app.packageName}/${app.activityName}"
            return hiddenKeys.contains(app.key) ||
                hiddenKeys.contains(componentKey) ||
                hiddenKeys.any { it.substringBefore('#') == componentKey }
        }

        val appsByComponentKey = allApps.associateBy { "${it.packageName}/${it.activityName}" }

        val renamedByKey = allApps.associate { app ->
            val componentKey = "${app.packageName}/${app.activityName}"
            val customName = customNames[app.key] ?: customNames[componentKey] ?: customNames[app.packageName]
            val effectiveLabel = if (!customName.isNullOrBlank()) customName else app.label
            app.key to app.copy(label = effectiveLabel, originalLabel = app.label)
        }

        val processedApps = allApps
            .filter { !isHiddenApp(it) }
            .mapNotNull { rawApp ->
                val app = renamedByKey[rawApp.key] ?: return@mapNotNull null
                val componentKey = "${app.packageName}/${app.activityName}"
                val subAppKeys = foldersMap[app.key]
                    ?: foldersMap[componentKey]
                    ?: foldersMap.entries.firstOrNull { it.key.substringBefore('#') == componentKey }?.value
                    ?: emptyList()
                val subApps = subAppKeys.mapNotNull { subKey ->
                    renamedByKey[subKey]
                        ?: appsByComponentKey[subKey.substringBefore('#')]?.let { renamedByKey[it.key] }
                }.distinctBy { it.key }
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
            hiddenApps = allApps.filter { isHiddenApp(it) }.mapNotNull { renamedByKey[it.key] }
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
        val isGerman = appLanguage.value.isGerman()
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                novaBackupImporter.importFromUri(uri, isGerman = isGerman)
            }
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

    /** Exportiert sämtliche Einstellungen, Favoriten, Ordner, Statistik und Custom-Icons als JSON. */
    fun exportLauncherBackup(uri: Uri, onComplete: (success: Boolean, error: String?) -> Unit) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    val iconFiles = importedIcons.exportAllAsBase64()
                    val data = favoritesRepository.exportBackupData(iconFiles)
                    val encoded = LauncherBackupCodec.encodeToJson(data)
                    val out = getApplication<Application>().contentResolver.openOutputStream(uri, "wt")
                        ?: error("Output stream unavailable")
                    out.bufferedWriter(Charsets.UTF_8).use { writer ->
                        writer.write(encoded)
                    }
                    encoded
                }
                if (json.isNotEmpty()) {
                    onComplete(true, null)
                } else {
                    onComplete(false, "Empty backup")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: e.javaClass.simpleName)
            }
        }
    }

    /** Stellt Einstellungen, Favoriten, Ordner, Statistik und Custom-Icons aus einer JSON-Sicherung wieder her. */
    fun importLauncherBackup(uri: Uri, onComplete: (success: Boolean, error: String?) -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val input = getApplication<Application>().contentResolver.openInputStream(uri)
                        ?: error("Input stream unavailable")
                    val json = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val data = LauncherBackupCodec.decodeFromJson(json)
                    importedIcons.importAllFromBase64(data.importedIconFiles)
                    favoritesRepository.restoreBackupData(data)
                    iconPackManager.setCustomOverrides(data.customIcons)
                }
                _customIconsCount.value = importedIcons.count()
                iconPackManager.clearIconCache()
                appRepository.refreshApps()
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: e.javaClass.simpleName)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        appRepository.stopListening()
        mediaHelper.stopListening()
    }

    private val fallbackNotifyingAppCache = ConcurrentHashMap<String, AppItem>()
    private val missingFallbackPackages = ConcurrentHashMap.newKeySet<String>()

    fun resolveNotifyingAppFallback(packageName: String): AppItem? {
        if (packageName in missingFallbackPackages) return null
        fallbackNotifyingAppCache[packageName]?.let { return it }

        val appContext = getApplication<Application>()
        val pm = appContext.packageManager
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val label = pm.getApplicationLabel(appInfo)?.toString()?.takeIf { it.isNotBlank() } ?: packageName
            val icon = iconPackManager.getAppIcon(packageName, "") {
                try {
                    pm.getApplicationIcon(appInfo)
                } catch (_: Exception) {
                    null
                }
            }
            val item = AppItem(
                packageName = packageName,
                activityName = "",
                label = label,
                userHandle = Process.myUserHandle(),
                icon = icon,
                isFavorite = false,
                isHidden = false
            )
            fallbackNotifyingAppCache[packageName] = item
            item
        } catch (_: Exception) {
            missingFallbackPackages.add(packageName)
            null
        }
    }

    fun launchApp(app: AppItem) {
        if (app.activityName.isNotBlank()) {
            val isFav = app.isFavorite || launcherState.value.favoriteApps.any { it.key == app.key }
            viewModelScope.launch {
                favoritesRepository.recordAppLaunch(app.key, isFavorite = isFav)
            }
        }
        onReturnToLauncher()
        if (app.activityName.isBlank()) {
            if (!LauncherNotificationService.openNotificationForPackage(app.packageName)) {
                appRepository.launchApp(app)
            }
        } else {
            appRepository.launchApp(app)
        }
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
        if (!open) {
            _isHiddenAppsOpen.value = false
            _isTypographyOpen.value = false
            _isUsageStatsOpen.value = false
        }
    }

    fun setHiddenAppsOpen(open: Boolean) {
        _isHiddenAppsOpen.value = open
    }

    fun setTypographyOpen(open: Boolean) {
        _isTypographyOpen.value = open
    }

    fun setUsageStatsOpen(open: Boolean) {
        _isUsageStatsOpen.value = open
    }

    fun setManageFavoritesOpen(open: Boolean) {
        _isManageFavoritesOpen.value = open
    }

    fun toggleShowBattery(show: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setShowBattery(show)
        }
    }

    fun toggleShowClock(show: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setShowClock(show)
        }
    }

    fun setClockSizeSp(sizeSp: Int) {
        viewModelScope.launch {
            favoritesRepository.setClockSizeSp(sizeSp)
        }
    }

    fun toggleShowDate(show: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setShowDate(show)
        }
    }

    fun toggleShowRecentApps(show: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setShowRecentApps(show)
        }
    }

    fun toggleShowAppIcons(show: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setShowAppIcons(show)
        }
    }

    fun toggleHideStatusBar(hide: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setHideStatusBar(hide)
        }
    }

    fun toggleShowBottomActions(show: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setShowBottomActions(show)
        }
    }

    fun toggleShowNotificationApps(show: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setShowNotificationApps(show)
        }
    }

    fun toggleShowNotificationPreview(show: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setShowNotificationPreview(show)
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

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch {
            favoritesRepository.setAppLanguage(language)
        }
    }

    fun setAppFont(font: LauncherFont) {
        viewModelScope.launch {
            favoritesRepository.setAppFont(font)
        }
    }

    fun setClockDateFont(font: LauncherFont) {
        viewModelScope.launch {
            favoritesRepository.setClockDateFont(font)
        }
    }

    fun setWaveFont(font: LauncherFont) {
        viewModelScope.launch {
            favoritesRepository.setWaveFont(font)
        }
    }

    fun setAppFontSizeSp(sizeSp: Int) {
        viewModelScope.launch {
            favoritesRepository.setAppFontSizeSp(sizeSp)
        }
    }

    fun setAppRowSpacingDp(spacingDp: Int) {
        viewModelScope.launch {
            favoritesRepository.setAppRowSpacingDp(spacingDp)
        }
    }

    fun setDateSizeSp(sizeSp: Int) {
        viewModelScope.launch {
            favoritesRepository.setDateSizeSp(sizeSp)
        }
    }

    fun setWaveFontSizeSp(sizeSp: Int) {
        viewModelScope.launch {
            favoritesRepository.setWaveFontSizeSp(sizeSp)
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
    fun refreshMediaListener() = mediaHelper.startListening()
    fun refreshNotifications() = LauncherNotificationService.syncOrRebind(getApplication())

    fun dismissNotificationsForApp(packageName: String) {
        LauncherNotificationService.dismissNotificationsForPackage(packageName)
    }

    fun openActiveMediaApp() {
        val pkg = mediaState.value.packageName ?: return
        val matchedApp = launcherState.value.allApps.firstOrNull { it.packageName == pkg }
        if (matchedApp != null) {
            launchApp(matchedApp)
            return
        }
        try {
            val intent = getApplication<Application>().packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                onReturnToLauncher()
                getApplication<Application>().startActivity(intent)
            }
        } catch (_: Exception) {}
    }
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
