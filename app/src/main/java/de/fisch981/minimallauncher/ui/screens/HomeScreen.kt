package de.fisch981.minimallauncher.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import de.fisch981.minimallauncher.data.model.AppItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fisch981.minimallauncher.data.model.AppNotificationItem
import de.fisch981.minimallauncher.ui.components.AppListItem
import de.fisch981.minimallauncher.ui.components.AppPopupSheet
import de.fisch981.minimallauncher.ui.components.FavoritesSelectionDialog
import de.fisch981.minimallauncher.ui.components.HeaderWidget
import de.fisch981.minimallauncher.ui.components.IconPickerDialog
import de.fisch981.minimallauncher.ui.components.RenameAppDialog
import de.fisch981.minimallauncher.ui.components.SearchOverlay
import de.fisch981.minimallauncher.ui.components.WaveScroller
import de.fisch981.minimallauncher.ui.theme.BorderDark
import de.fisch981.minimallauncher.ui.theme.EnglishStrings
import de.fisch981.minimallauncher.ui.theme.GermanStrings
import de.fisch981.minimallauncher.ui.theme.GrayLight
import de.fisch981.minimallauncher.ui.theme.GrayMedium
import de.fisch981.minimallauncher.ui.theme.LocalAppStrings
import de.fisch981.minimallauncher.ui.theme.PureBlack
import de.fisch981.minimallauncher.ui.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val systemLocale = remember(configuration) {
        configuration.locales.get(0) ?: Locale.getDefault()
    }
    val strings = remember(appLanguage, systemLocale) {
        if (appLanguage.isGerman(systemLocale)) GermanStrings else EnglishStrings
    }

    val uiState by viewModel.launcherState.collectAsState()
    val mediaState by viewModel.mediaState.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val scrollerSide by viewModel.scrollerSide.collectAsState()
    val hasLeftWave = scrollerSide.hasLeft
    val hasRightWave = scrollerSide.hasRight
    val waveIdleAlphaPercent by viewModel.waveIdleAlphaPercent.collectAsState()
    val showClock by viewModel.showClock.collectAsState()
    val clockSizeSp by viewModel.clockSizeSp.collectAsState()
    val showDate by viewModel.showDate.collectAsState()
    val showBattery by viewModel.showBattery.collectAsState()
    val showMedia by viewModel.showMedia.collectAsState()
    val showRecentApps by viewModel.showRecentApps.collectAsState()
    val showAppIcons by viewModel.showAppIcons.collectAsState()
    val showBottomActions by viewModel.showBottomActions.collectAsState()
    val showNotificationPreview by viewModel.showNotificationPreview.collectAsState()
    val appFont by viewModel.appFont.collectAsState()
    val clockDateFont by viewModel.clockDateFont.collectAsState()
    val waveFont by viewModel.waveFont.collectAsState()
    val appFontSizeSp by viewModel.appFontSizeSp.collectAsState()
    val appRowSpacingDp by viewModel.appRowSpacingDp.collectAsState()
    val dateSizeSp by viewModel.dateSizeSp.collectAsState()
    val waveFontSizeSp by viewModel.waveFontSizeSp.collectAsState()

    val appFontFamily = remember(appFont) { appFont.toFontFamily() }
    val clockDateFontFamily = remember(clockDateFont) { clockDateFont.toFontFamily() }
    val waveFontFamily = remember(waveFont) { waveFont.toFontFamily() }

    val selectedAppForPopup by viewModel.selectedAppForPopup.collectAsState()
    val selectedShortcuts by viewModel.selectedAppShortcuts.collectAsState()
    val isSearchOpen by viewModel.isSearchOpen.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isManageFavoritesOpen by viewModel.isManageFavoritesOpen.collectAsState()
    val appForIconEditing by viewModel.appForIconEditing.collectAsState()
    val appForFolderEditing by viewModel.appForFolderEditing.collectAsState()
    val appForRenaming by viewModel.appForRenaming.collectAsState()
    val scrollToTopEvent by viewModel.scrollToTopEvent.collectAsState()

    // Mode: Home (Favorites only) vs App Drawer (All apps A-Z)
    var isAppDrawerMode by remember { mutableStateOf(false) }
    var isRecentExpanded by remember { mutableStateOf(false) }
    var isLeftWaveTouching by remember { mutableStateOf(false) }
    val waveClearanceOffset by animateDpAsState(
        targetValue = if (hasLeftWave && isLeftWaveTouching) 76.dp else 0.dp,
        label = "waveClearanceOffset"
    )

    val homeListState = rememberLazyListState()
    val drawerListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Back button / return to launcher returns from App Drawer to Favorites Home & collapses recent list
    LaunchedEffect(scrollToTopEvent) {
        if (scrollToTopEvent > 0) {
            if (isAppDrawerMode) {
                isAppDrawerMode = false
            }
            isRecentExpanded = false
            homeListState.scrollToItem(0)
        }
    }

    LaunchedEffect(isRecentExpanded) {
        if (!isRecentExpanded) {
            homeListState.scrollToItem(0)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkDefaultLauncher()
    }

    // Index map for all-apps LazyColumn (index 0 is top spacer)
    val letterIndexMap = remember(uiState.allApps) {
        val map = mutableMapOf<Char, Int>()
        var currentIndex = 1 // top spacer
        var lastChar: Char? = null

        for (app in uiState.allApps) {
            val char = app.initialChar
            if (char != lastChar) {
                map[char] = currentIndex
                currentIndex++ // section letter header
                lastChar = char
            }
            currentIndex++ // app item
        }
        map
    }

    val alphabetOrder = remember { ('A'..'Z').toList() + listOf('#') }

    fun scrollDrawerToLetter(letter: Char) {
        if (letter == '★') {
            isAppDrawerMode = false
            return
        }
        isAppDrawerMode = true
        val exact = letterIndexMap[letter]
        val targetIndex = if (exact != null) {
            exact
        } else {
            val startPos = alphabetOrder.indexOf(letter).coerceAtLeast(0)
            var found: Int? = null
            for (i in startPos until alphabetOrder.size) {
                val candidate = letterIndexMap[alphabetOrder[i]]
                if (candidate != null) {
                    found = candidate
                    break
                }
            }
            found ?: 0
        }
        coroutineScope.launch {
            drawerListState.scrollToItem(targetIndex)
        }
    }

    var pendingIconEditApp by remember { mutableStateOf<AppItem?>(null) }
    val externalIconPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val targetApp = pendingIconEditApp
        pendingIconEditApp = null
        val data = result.data
        if (targetApp != null && data != null) {
            viewModel.applyPickedIconFromResult(targetApp, data) { saved ->
                if (!saved && result.resultCode == Activity.RESULT_OK) {
                    Toast.makeText(context, strings.selectedIconReadError, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    CompositionLocalProvider(LocalAppStrings provides strings) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Main Content Area (Favorites Home OR App Drawer A-Z)
        if (!isAppDrawerMode) {
            // ==========================================
            // MODE A: HOMESCREEN (NUR FAVORITEN)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = waveClearanceOffset)
            ) {
                LazyColumn(
                    state = homeListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    userScrollEnabled = homeListState.canScrollForward || homeListState.canScrollBackward
                ) {
                    item(key = "home_header") {
                        HeaderWidget(
                            mediaState = mediaState,
                            onMediaPlayPause = { viewModel.mediaPlayPause() },
                            onMediaNext = { viewModel.mediaNext() },
                            onMediaPrevious = { viewModel.mediaPrevious() },
                            onMediaOpenApp = { viewModel.openActiveMediaApp() },
                            showClock = showClock,
                            clockSizeSp = clockSizeSp,
                            showDate = showDate,
                            dateSizeSp = dateSizeSp,
                            fontFamily = clockDateFontFamily,
                            showBattery = showBattery,
                            showMedia = showMedia,
                            hasLeftWave = hasLeftWave,
                            hasRightWave = hasRightWave
                        )
                    }

                    items(
                        items = uiState.favoriteApps,
                        key = { "home_fav_${it.key}" }
                    ) { app ->
                        AppListItem(
                            app = app,
                            notifications = notifications[app.packageName] ?: emptyList(),
                            onClick = { viewModel.launchApp(app) },
                            onLongClick = { viewModel.openPopup(app) },
                            onSwipeRight = { viewModel.openPopup(app) },
                            showIcon = showAppIcons,
                            showNotificationPreview = showNotificationPreview,
                            fontFamily = appFontFamily,
                            fontSizeSp = appFontSizeSp,
                            verticalPaddingDp = appRowSpacingDp,
                            hasLeftWave = hasLeftWave,
                            hasRightWave = hasRightWave
                        )
                    }

                    if (showRecentApps && uiState.recentApps.isNotEmpty()) {
                        item(key = "home_recent_apps") {
                            RecentAppsEntry(
                                recentApps = uiState.recentApps,
                                notifications = notifications,
                                isExpanded = isRecentExpanded,
                                onToggleExpand = { isRecentExpanded = !isRecentExpanded },
                                onLaunchApp = {
                                    isRecentExpanded = false
                                    viewModel.launchApp(it)
                                },
                                onLongClickApp = { viewModel.openPopup(it) },
                                showIcon = showAppIcons,
                                showNotificationPreview = showNotificationPreview,
                                appFontFamily = appFontFamily,
                                appFontSizeSp = appFontSizeSp,
                                appRowSpacingDp = appRowSpacingDp,
                                hasLeftWave = hasLeftWave,
                                hasRightWave = hasRightWave
                            )
                        }
                    }
                }

                // Dezente Schnellaktionen unten links (fester Fußbereich ohne Überlagerung)
                if (showBottomActions) {
                    Row(
                        modifier = Modifier
                            .padding(
                                start = if (hasLeftWave) 72.dp else 28.dp,
                                top = 0.dp,
                                bottom = 8.dp
                            )
                            .alpha(0.65f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { viewModel.setSearchOpen(true) }
                                .padding(end = 18.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = strings.searchAppsButton,
                                tint = GrayMedium,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.searchAppsButton,
                                fontSize = 12.sp,
                                color = GrayMedium
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.settingsContentDesc,
                            tint = GrayMedium,
                            modifier = Modifier
                                .clickable { viewModel.setSettingsOpen(true) }
                                .padding(4.dp)
                                .size(16.dp)
                        )
                    }
                }
            }
        } else {
            // ==========================================
            // MODE B: APP DRAWER (ALLE APPS A-Z)
            // ==========================================
            LazyColumn(
                state = drawerListState,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = waveClearanceOffset)
            ) {
                item(key = "drawer_top_spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                var currentSectionChar: Char? = null
                for (app in uiState.allApps) {
                    val char = app.initialChar
                    if (char != currentSectionChar) {
                        currentSectionChar = char
                        item(key = "drawer_header_$char") {
                            Text(
                                text = char.toString(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = appFontFamily,
                                color = GrayMedium,
                                modifier = Modifier.padding(
                                    start = if (hasLeftWave) 72.dp else 28.dp,
                                    top = 16.dp,
                                    bottom = 4.dp
                                )
                            )
                        }
                    }

                    item(key = "drawer_app_${app.key}") {
                        AppListItem(
                            app = app,
                            notifications = notifications[app.packageName] ?: emptyList(),
                            onClick = { viewModel.launchApp(app) },
                            onLongClick = { viewModel.openPopup(app) },
                            onSwipeRight = { viewModel.openPopup(app) },
                            showIcon = showAppIcons,
                            showNotificationPreview = showNotificationPreview,
                            fontFamily = appFontFamily,
                            fontSizeSp = appFontSizeSp,
                            verticalPaddingDp = appRowSpacingDp,
                            hasLeftWave = hasLeftWave,
                            hasRightWave = hasRightWave
                        )
                    }
                }

                // Footer am Ende der App-Liste
                item(key = "drawer_footer") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = if (hasLeftWave) 72.dp else 28.dp,
                                end = if (hasRightWave) 56.dp else 24.dp,
                                top = 24.dp,
                                bottom = 64.dp
                            )
                    ) {
                        HorizontalDivider(color = BorderDark, modifier = Modifier.padding(bottom = 16.dp))

                        ActionRowButton(
                            icon = Icons.Default.Search,
                            label = strings.searchAppsButton,
                            onClick = { viewModel.setSearchOpen(true) }
                        )
                        ActionRowButton(
                            icon = Icons.Default.Star,
                            label = strings.editFavorites,
                            onClick = { viewModel.setManageFavoritesOpen(true) }
                        )
                        ActionRowButton(
                            icon = Icons.Default.Settings,
                            label = strings.launcherSettings,
                            onClick = { viewModel.setSettingsOpen(true) }
                        )
                    }
                }
            }
        }

        // WaveScroller links (bei LINKS oder BEIDSEITIG)
        if (hasLeftWave) {
            WaveScroller(
                isRightHanded = false,
                availableLetters = uiState.availableLetters,
                onLetterSelected = { letter -> scrollDrawerToLetter(letter) },
                onTouchStateChanged = { touching ->
                    isLeftWaveTouching = touching
                    if (touching) viewModel.recordWaveUsage(isRightSide = false)
                },
                idleAlphaPercent = waveIdleAlphaPercent,
                fontFamily = waveFontFamily,
                letterFontSizeSp = waveFontSizeSp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer { clip = false }
            )
        }

        // WaveScroller rechts (bei RECHTS oder BEIDSEITIG)
        if (hasRightWave) {
            WaveScroller(
                isRightHanded = true,
                availableLetters = uiState.availableLetters,
                onLetterSelected = { letter -> scrollDrawerToLetter(letter) },
                onTouchStateChanged = { touching ->
                    if (touching) viewModel.recordWaveUsage(isRightSide = true)
                },
                idleAlphaPercent = waveIdleAlphaPercent,
                fontFamily = waveFontFamily,
                letterFontSizeSp = waveFontSizeSp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .graphicsLayer { clip = false }
            )
        }

        // App Popup Sheet
        val currentApp = selectedAppForPopup
        if (currentApp != null) {
            AppPopupSheet(
                app = currentApp,
                shortcuts = selectedShortcuts,
                notifications = notifications[currentApp.packageName] ?: emptyList(),
                isFavorite = currentApp.isFavorite,
                onDismiss = { viewModel.closePopup() },
                onToggleFavorite = { viewModel.toggleFavorite(currentApp) },
                onRenameAppClick = { viewModel.openRenameDialog(currentApp) },
                onEditIconClick = {
                    val pickerIntent = viewModel.iconPackManager.createExternalIconPickerIntent()
                    if (pickerIntent != null) {
                        pendingIconEditApp = currentApp
                        try {
                            externalIconPickerLauncher.launch(pickerIntent)
                        } catch (_: Exception) {
                            pendingIconEditApp = null
                            viewModel.openIconEditor(currentApp)
                        }
                    } else {
                        viewModel.openIconEditor(currentApp)
                    }
                },
                onResetIconClick = { viewModel.resetAppIcon(currentApp) },
                onHideApp = { viewModel.hideApp(currentApp) },
                onLaunchShortcut = { viewModel.launchShortcut(it) },
                onLaunchSubApp = { viewModel.launchApp(it) },
                onAddSubAppClick = { viewModel.openFolderEditor(currentApp) }
            )
        }

        // Icon Picker Dialog
        val editingApp = appForIconEditing
        if (editingApp != null) {
            IconPickerDialog(
                app = editingApp,
                iconPackManager = viewModel.iconPackManager,
                onSelectIcon = { drawableName ->
                    viewModel.setCustomAppIcon(editingApp, drawableName)
                },
                onResetDefault = {
                    viewModel.resetAppIcon(editingApp)
                },
                onDismiss = { viewModel.closeIconEditor() }
            )
        }

        // Pop-up Folder Editor Dialog
        val folderApp = appForFolderEditing
        if (folderApp != null) {
            val latestFolderApp = uiState.allApps.firstOrNull { it.key == folderApp.key } ?: folderApp
            FavoritesSelectionDialog(
                allApps = uiState.allApps.filter { it.key != latestFolderApp.key },
                favoriteKeys = latestFolderApp.subApps.map { it.key }.toSet(),
                onToggleFavorite = { child -> viewModel.toggleSubApp(latestFolderApp, child) },
                onDismiss = { viewModel.closeFolderEditor() },
                title = strings.folderForTitle(latestFolderApp.label)
            )
        }

        // Rename App Dialog
        val renamingApp = appForRenaming
        if (renamingApp != null) {
            RenameAppDialog(
                app = renamingApp,
                onSave = { newName -> viewModel.renameApp(renamingApp, newName) },
                onDismiss = { viewModel.closeRenameDialog() }
            )
        }

        // Manage Favorites Dialog
        if (isManageFavoritesOpen) {
            FavoritesSelectionDialog(
                allApps = uiState.allApps,
                favoriteKeys = uiState.favoriteApps.map { it.key }.toSet(),
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onDismiss = { viewModel.setManageFavoritesOpen(false) }
            )
        }

        // Search Overlay
        if (isSearchOpen) {
            SearchOverlay(
                allApps = uiState.allApps,
                onAppClick = { app ->
                    viewModel.setSearchOpen(false)
                    viewModel.launchApp(app)
                },
                onOpenSettings = {
                    viewModel.setSearchOpen(false)
                    viewModel.setSettingsOpen(true)
                },
                onClose = { viewModel.setSearchOpen(false) },
                showAppIcons = showAppIcons,
                appFontFamily = appFontFamily,
                appFontSizeSp = appFontSizeSp,
                appRowSpacingDp = appRowSpacingDp
            )
        }

        // Settings Screen
        if (isSettingsOpen) {
            SettingsScreen(
                viewModel = viewModel,
                onClose = { viewModel.setSettingsOpen(false) }
            )
        }
    }
    }
}

@Composable
private fun ActionRowButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = GrayLight,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = GrayLight
        )
    }
}

@Composable
private fun RecentAppsEntry(
    recentApps: List<AppItem>,
    notifications: Map<String, List<AppNotificationItem>>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onLongClickApp: (AppItem) -> Unit,
    showIcon: Boolean,
    showNotificationPreview: Boolean,
    appFontFamily: FontFamily,
    appFontSizeSp: Int,
    appRowSpacingDp: Int,
    hasLeftWave: Boolean,
    hasRightWave: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val startPadding = if (hasLeftWave) 72.dp else 28.dp
    val endPadding = if (hasRightWave) 56.dp else 24.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(start = startPadding, end = endPadding, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = strings.recentlyUsed,
                tint = GrayLight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = strings.recentlyUsed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = appFontFamily,
                color = GrayLight
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) strings.collapse else strings.expand,
                tint = GrayMedium,
                modifier = Modifier.size(18.dp)
            )
        }

        if (isExpanded) {
            recentApps.take(5).forEach { recentApp ->
                AppListItem(
                    app = recentApp,
                    notifications = notifications[recentApp.packageName] ?: emptyList(),
                    onClick = { onLaunchApp(recentApp) },
                    onLongClick = { onLongClickApp(recentApp) },
                    onSwipeRight = { onLongClickApp(recentApp) },
                    showIcon = showIcon,
                    showNotificationPreview = showNotificationPreview,
                    fontFamily = appFontFamily,
                    fontSizeSp = appFontSizeSp,
                    verticalPaddingDp = appRowSpacingDp,
                    hasLeftWave = hasLeftWave,
                    hasRightWave = hasRightWave
                )
            }
        }
    }
}
