package de.fisch981.minimallauncher.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fisch981.minimallauncher.data.model.AppItem
import de.fisch981.minimallauncher.data.model.HomeGestureAction
import de.fisch981.minimallauncher.data.model.ScrollerSide
import de.fisch981.minimallauncher.data.repository.LauncherUtils
import de.fisch981.minimallauncher.data.service.LauncherNotificationService
import de.fisch981.minimallauncher.ui.theme.AppLanguage
import de.fisch981.minimallauncher.ui.theme.BorderDark
import de.fisch981.minimallauncher.ui.theme.DarkSurfaceVariant
import de.fisch981.minimallauncher.ui.theme.GrayLight
import de.fisch981.minimallauncher.ui.theme.GrayMedium
import de.fisch981.minimallauncher.ui.theme.LauncherFont
import de.fisch981.minimallauncher.ui.theme.LocalAppStrings
import de.fisch981.minimallauncher.ui.theme.PureBlack
import de.fisch981.minimallauncher.ui.theme.PureWhite
import de.fisch981.minimallauncher.ui.viewmodel.LauncherViewModel
import de.fisch981.minimallauncher.ui.viewmodel.SettingsCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current

    val activeSettingsCategory by viewModel.activeSettingsCategory.collectAsState()
    val isHiddenAppsOpen by viewModel.isHiddenAppsOpen.collectAsState()
    val uiState by viewModel.launcherState.collectAsState()

    var roleRequestLaunchTimeMs by remember { mutableLongStateOf(0L) }
    val roleRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkDefaultLauncher()
        if (!LauncherUtils.isDefaultLauncher(context) &&
            System.currentTimeMillis() - roleRequestLaunchTimeMs < 350L
        ) {
            LauncherUtils.openSystemDefaultLauncherSettings(context)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importNovaBackup(uri) { count, error ->
                if (error != null) {
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        context,
                        strings.novaImportSuccessToast(count),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportLauncherBackup(uri) { success, error ->
                val message = if (success) {
                    strings.backupExportSuccessToast
                } else {
                    strings.backupExportErrorToast(error ?: "Unknown error")
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importLauncherBackup(uri) { success, error ->
                val message = if (success) {
                    strings.backupImportSuccessToast
                } else {
                    strings.backupImportErrorToast(error ?: "Unknown error")
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack)
                .padding(top = 16.dp)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = PureWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.settingsTitle,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                item {
                    SettingsCategoryRow(
                        icon = Icons.Default.Apps,
                        title = strings.sectionHomeApps,
                        subtitle = strings.categoryHomeAppsSubtitle,
                        onClick = { viewModel.openSettingsCategory(SettingsCategory.HOME_APPS) }
                    )
                    HorizontalDivider(color = BorderDark)

                    SettingsCategoryRow(
                        icon = Icons.Default.Schedule,
                        title = strings.categoryHeaderWidgetsTitle,
                        subtitle = strings.categoryHeaderWidgetsSubtitle,
                        onClick = { viewModel.openSettingsCategory(SettingsCategory.HEADER_WIDGETS) }
                    )
                    HorizontalDivider(color = BorderDark)

                    SettingsCategoryRow(
                        icon = Icons.Default.Notifications,
                        title = strings.categoryNotificationsTitle,
                        subtitle = strings.categoryNotificationsSubtitle,
                        onClick = { viewModel.openSettingsCategory(SettingsCategory.NOTIFICATIONS) }
                    )
                    HorizontalDivider(color = BorderDark)

                    SettingsCategoryRow(
                        icon = Icons.Default.TextFields,
                        title = strings.typographyTitle,
                        subtitle = strings.typographySubtitle,
                        onClick = { viewModel.openSettingsCategory(SettingsCategory.TYPOGRAPHY) }
                    )
                    HorizontalDivider(color = BorderDark)

                    SettingsCategoryRow(
                        icon = Icons.Default.Tune,
                        title = strings.categoryIconsWaveTitle,
                        subtitle = strings.categoryIconsWaveSubtitle,
                        onClick = { viewModel.openSettingsCategory(SettingsCategory.ICONS_WAVE) }
                    )
                    HorizontalDivider(color = BorderDark)

                    SettingsCategoryRow(
                        icon = Icons.Default.BarChart,
                        title = strings.sectionUsageStats,
                        subtitle = strings.usageStatsSubtitle,
                        onClick = { viewModel.openSettingsCategory(SettingsCategory.USAGE_STATS) }
                    )
                    HorizontalDivider(color = BorderDark)

                    SettingsCategoryRow(
                        icon = Icons.Default.Settings,
                        title = strings.categorySystemBackupTitle,
                        subtitle = strings.categorySystemBackupSubtitle,
                        onClick = { viewModel.openSettingsCategory(SettingsCategory.SYSTEM_BACKUP) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "MinimalLauncher v1.1.1",
                        color = GrayMedium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
            }
        }

        when (activeSettingsCategory) {
            SettingsCategory.HOME_APPS -> HomeAppsSubScreen(
                viewModel = viewModel,
                onBack = { viewModel.openSettingsCategory(null) }
            )
            SettingsCategory.HEADER_WIDGETS -> HeaderWidgetsSubScreen(
                viewModel = viewModel,
                onBack = { viewModel.openSettingsCategory(null) }
            )
            SettingsCategory.NOTIFICATIONS -> NotificationsSubScreen(
                viewModel = viewModel,
                onBack = { viewModel.openSettingsCategory(null) }
            )
            SettingsCategory.TYPOGRAPHY -> TypographySubScreen(
                viewModel = viewModel,
                onBack = { viewModel.openSettingsCategory(null) }
            )
            SettingsCategory.ICONS_WAVE -> IconsWaveSubScreen(
                viewModel = viewModel,
                onBack = { viewModel.openSettingsCategory(null) }
            )
            SettingsCategory.USAGE_STATS -> UsageStatsSubScreen(
                viewModel = viewModel,
                onBack = { viewModel.openSettingsCategory(null) }
            )
            SettingsCategory.SYSTEM_BACKUP -> SystemBackupSubScreen(
                viewModel = viewModel,
                onRequestDefaultLauncher = {
                    roleRequestLaunchTimeMs = System.currentTimeMillis()
                    val roleIntent = LauncherUtils.createRoleRequestIntent(context)
                    if (roleIntent != null) {
                        try {
                            roleRequestLauncher.launch(roleIntent)
                        } catch (_: Exception) {
                            LauncherUtils.openSystemDefaultLauncherSettings(context)
                        }
                    } else {
                        LauncherUtils.openSystemDefaultLauncherSettings(context)
                    }
                },
                onExportBackup = {
                    try {
                        val dateStamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        exportBackupLauncher.launch("minimallauncher-backup-$dateStamp.json")
                    } catch (_: Exception) {}
                },
                onImportBackup = {
                    try {
                        importBackupLauncher.launch(arrayOf("application/json", "*/*"))
                    } catch (_: Exception) {}
                },
                onImportNovaBackup = {
                    try {
                        filePickerLauncher.launch(arrayOf("*/*"))
                    } catch (_: Exception) {}
                },
                onBack = { viewModel.openSettingsCategory(null) }
            )
            null -> Unit
        }

        if (isHiddenAppsOpen) {
            HiddenAppsSubScreen(
                hiddenApps = uiState.hiddenApps,
                onUnhideApp = { viewModel.unhideApp(it) },
                onBack = { viewModel.setHiddenAppsOpen(false) }
            )
        }
    }
}

@Composable
private fun SettingsCategoryRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PureWhite,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = GrayMedium,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = GrayMedium,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSubPageScaffold(
    title: String,
    onBack: () -> Unit,
    content: LazyListScope.() -> Unit
) {
    val strings = LocalAppStrings.current
    Surface(
        color = PureBlack,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = PureWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                content = content
            )
        }
    }
}

@Composable
private fun HomeAppsSubScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val uiState by viewModel.launcherState.collectAsState()
    val showRecentApps by viewModel.showRecentApps.collectAsState()
    val showBottomActions by viewModel.showBottomActions.collectAsState()
    val appLongPressDelayMs by viewModel.appLongPressDelayMs.collectAsState()
    val homeSwipeDownAction by viewModel.homeSwipeDownAction.collectAsState()
    val homeSwipeUpAction by viewModel.homeSwipeUpAction.collectAsState()
    val homeDoubleTapAction by viewModel.homeDoubleTapAction.collectAsState()

    SettingsSubPageScaffold(title = strings.sectionHomeApps, onBack = onBack) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingItem(
                title = strings.manageFavoritesTitle,
                subtitle = strings.manageFavoritesSubtitle(uiState.favoriteApps.size),
                onClick = { viewModel.setManageFavoritesOpen(true) }
            )

            SettingSwitchItem(
                title = strings.showRecentAppsTitle,
                subtitle = strings.showRecentAppsSubtitle,
                checked = showRecentApps,
                onCheckedChange = { viewModel.toggleShowRecentApps(it) }
            )

            SettingItem(
                title = strings.hiddenAppsTitle,
                subtitle = if (uiState.hiddenApps.isEmpty()) {
                    strings.noHiddenApps
                } else {
                    strings.hiddenAppsManageSubtitle(uiState.hiddenApps.size)
                },
                onClick = { viewModel.setHiddenAppsOpen(true) }
            )

            SettingSwitchItem(
                title = strings.showBottomActionsTitle,
                subtitle = strings.showBottomActionsSubtitle,
                checked = showBottomActions,
                onCheckedChange = { viewModel.toggleShowBottomActions(it) }
            )

            HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        strings.appLongPressDelayTitle,
                        color = PureWhite,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${appLongPressDelayMs} ms",
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    strings.appLongPressDelaySubtitle,
                    color = GrayMedium,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = appLongPressDelayMs.toFloat(),
                    onValueChange = { raw ->
                        val snapped = ((raw / 50f).roundToInt() * 50).coerceIn(250, 2000)
                        viewModel.setAppLongPressDelayMs(snapped)
                    },
                    valueRange = 250f..2000f,
                    colors = SliderDefaults.colors(
                        thumbColor = PureWhite,
                        activeTrackColor = PureWhite,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )
            }

            HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
            SectionHeader(strings.sectionHomeGestures)

            HomeGestureSelector(
                title = strings.gestureSwipeDownTitle,
                subtitle = strings.gestureSwipeDownSubtitle,
                selectedAction = homeSwipeDownAction,
                onSelectAction = { viewModel.setHomeSwipeDownAction(it) }
            )

            HomeGestureSelector(
                title = strings.gestureSwipeUpTitle,
                subtitle = strings.gestureSwipeUpSubtitle,
                selectedAction = homeSwipeUpAction,
                onSelectAction = { viewModel.setHomeSwipeUpAction(it) }
            )

            HomeGestureSelector(
                title = strings.gestureDoubleTapTitle,
                subtitle = strings.gestureDoubleTapSubtitle,
                selectedAction = homeDoubleTapAction,
                onSelectAction = { viewModel.setHomeDoubleTapAction(it) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeGestureSelector(
    title: String,
    subtitle: String,
    selectedAction: HomeGestureAction,
    onSelectAction: (HomeGestureAction) -> Unit
) {
    val strings = LocalAppStrings.current
    val options = remember(strings) {
        listOf(
            HomeGestureAction.NONE to strings.gestureActionNone,
            HomeGestureAction.EXPAND_NOTIFICATIONS to strings.gestureActionNotifications,
            HomeGestureAction.SEARCH to strings.gestureActionSearch,
            HomeGestureAction.ALL_APPS to strings.gestureActionAllApps
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(title, color = PureWhite, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(subtitle, color = GrayMedium, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { (actionOption, actionLabel) ->
                        val selected = selectedAction == actionOption
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) PureWhite else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (selected) PureWhite else BorderDark,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectAction(actionOption) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = actionLabel,
                                color = if (selected) PureBlack else GrayLight,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderWidgetsSubScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val showClock by viewModel.showClock.collectAsState()
    val showDate by viewModel.showDate.collectAsState()
    val showBattery by viewModel.showBattery.collectAsState()
    val showNextAlarm by viewModel.showNextAlarm.collectAsState()
    val showMedia by viewModel.showMedia.collectAsState()
    val hideStatusBar by viewModel.hideStatusBar.collectAsState()

    SettingsSubPageScaffold(title = strings.categoryHeaderWidgetsTitle, onBack = onBack) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingSwitchItem(
                title = strings.showClockTitle,
                subtitle = strings.showClockSubtitle,
                checked = showClock,
                onCheckedChange = { viewModel.toggleShowClock(it) }
            )

            SettingSwitchItem(
                title = strings.showDateTitle,
                subtitle = strings.showDateSubtitle,
                checked = showDate,
                onCheckedChange = { viewModel.toggleShowDate(it) }
            )

            SettingSwitchItem(
                title = strings.showBatteryTitle,
                subtitle = strings.showBatterySubtitle,
                checked = showBattery,
                onCheckedChange = { viewModel.toggleShowBattery(it) }
            )

            SettingSwitchItem(
                title = strings.showNextAlarmTitle,
                subtitle = strings.showNextAlarmSubtitle,
                checked = showNextAlarm,
                onCheckedChange = { viewModel.toggleShowNextAlarm(it) }
            )

            SettingSwitchItem(
                title = strings.showMediaTitle,
                subtitle = strings.showMediaSubtitle,
                checked = showMedia,
                onCheckedChange = { viewModel.toggleShowMedia(it) }
            )

            SettingSwitchItem(
                title = strings.hideStatusBarTitle,
                subtitle = strings.hideStatusBarSubtitle,
                checked = hideStatusBar,
                onCheckedChange = { viewModel.toggleHideStatusBar(it) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NotificationsSubScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val showNotificationApps by viewModel.showNotificationApps.collectAsState()
    val showNotificationPreview by viewModel.showNotificationPreview.collectAsState()
    val showNotificationCount by viewModel.showNotificationCount.collectAsState()
    val isNotificationGranted = LauncherNotificationService.isPermissionGranted(context)

    SettingsSubPageScaffold(title = strings.categoryNotificationsTitle, onBack = onBack) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingSwitchItem(
                title = strings.showNotificationAppsTitle,
                subtitle = strings.showNotificationAppsSubtitle,
                checked = showNotificationApps,
                onCheckedChange = { viewModel.toggleShowNotificationApps(it) }
            )

            SettingSwitchItem(
                title = strings.showNotificationPreviewTitle,
                subtitle = strings.showNotificationPreviewSubtitle,
                checked = showNotificationPreview,
                onCheckedChange = { viewModel.toggleShowNotificationPreview(it) }
            )

            SettingSwitchItem(
                title = strings.showNotificationCountTitle,
                subtitle = strings.showNotificationCountSubtitle,
                checked = showNotificationCount,
                onCheckedChange = { viewModel.toggleShowNotificationCount(it) }
            )

            HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))

            SettingItem(
                title = strings.notificationAccessTitle,
                subtitle = if (isNotificationGranted) strings.notificationAccessGranted else strings.notificationAccessDenied,
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    } catch (_: Exception) {}
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IconsWaveSubScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val showAppIcons by viewModel.showAppIcons.collectAsState()
    val scrollerSide by viewModel.scrollerSide.collectAsState()
    val waveIdleAlphaPercent by viewModel.waveIdleAlphaPercent.collectAsState()
    val isLinesInstalled = viewModel.iconPackManager.isLinesPackInstalled()

    SettingsSubPageScaffold(title = strings.categoryIconsWaveTitle, onBack = onBack) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingSwitchItem(
                title = strings.showAppIconsTitle,
                subtitle = strings.showAppIconsSubtitle,
                checked = showAppIcons,
                onCheckedChange = { viewModel.toggleShowAppIcons(it) }
            )

            SettingItem(
                title = strings.linesPackStatusTitle,
                subtitle = if (isLinesInstalled) strings.linesPackActiveSubtitle else strings.linesPackInactiveSubtitle,
                onClick = { viewModel.reloadIcons() }
            )

            HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))

            // Alphabet Wave Position: Left / Right / Both
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(strings.wavePositionTitle, color = PureWhite, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    strings.wavePositionSubtitle,
                    color = GrayMedium,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val options = listOf(
                        ScrollerSide.LEFT to strings.sideLeft,
                        ScrollerSide.RIGHT to strings.sideRight,
                        ScrollerSide.BOTH to strings.sideBoth
                    )
                    for ((option, label) in options) {
                        val selected = scrollerSide == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) PureWhite else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (selected) PureWhite else BorderDark,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setScrollerSide(option) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) PureBlack else GrayLight,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Idle letter brightness
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        strings.waveIdleBrightnessTitle,
                        color = PureWhite,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${waveIdleAlphaPercent}%",
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    strings.waveIdleBrightnessSubtitle,
                    color = GrayMedium,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = waveIdleAlphaPercent.toFloat(),
                    onValueChange = { viewModel.setWaveIdleAlphaPercent(it.roundToInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = PureWhite,
                        activeTrackColor = PureWhite,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SystemBackupSubScreen(
    viewModel: LauncherViewModel,
    onRequestDefaultLauncher: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onImportNovaBackup: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val customIconsCount by viewModel.customIconsCountState.collectAsState()

    SettingsSubPageScaffold(title = strings.categorySystemBackupTitle, onBack = onBack) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(strings.sectionDefaultApp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.defaultLauncherTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            text = if (isDefaultLauncher) strings.defaultLauncherActive else strings.defaultLauncherInactive,
                            fontSize = 12.sp,
                            color = GrayMedium
                        )
                    }
                    Button(
                        onClick = onRequestDefaultLauncher,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = PureBlack
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isDefaultLauncher) strings.changeButton else strings.setButton,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))

            // App Language: System / Deutsch / English
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(strings.languageTitle, color = PureWhite, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    strings.languageSubtitle,
                    color = GrayMedium,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val languageOptions = listOf(
                        AppLanguage.SYSTEM to strings.languageSystem,
                        AppLanguage.DE to strings.languageGerman,
                        AppLanguage.EN to strings.languageEnglish
                    )
                    for ((option, label) in languageOptions) {
                        val selected = appLanguage == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) PureWhite else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (selected) PureWhite else BorderDark,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setAppLanguage(option) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) PureBlack else GrayLight,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
            SectionHeader(strings.sectionBackupImport)

            SettingItem(
                title = strings.exportBackupTitle,
                subtitle = strings.exportBackupSubtitle,
                onClick = onExportBackup
            )

            SettingItem(
                title = strings.importBackupTitle,
                subtitle = strings.importBackupSubtitle,
                onClick = onImportBackup
            )

            SettingItem(
                title = strings.selectNovaBackupTitle,
                subtitle = if (customIconsCount > 0) {
                    strings.novaCustomMappingsActive(customIconsCount)
                } else {
                    strings.novaBackupSubtitleDefault
                },
                onClick = onImportNovaBackup
            )

            if (customIconsCount > 0) {
                SettingItem(
                    title = strings.resetNovaMappingsTitle,
                    subtitle = strings.resetNovaMappingsSubtitle(customIconsCount),
                    onClick = {
                        viewModel.clearCustomIcons()
                        Toast.makeText(context, strings.novaMappingsResetToast, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TypographySubScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val appFont by viewModel.appFont.collectAsState()
    val clockDateFont by viewModel.clockDateFont.collectAsState()
    val waveFont by viewModel.waveFont.collectAsState()
    val appFontSizeSp by viewModel.appFontSizeSp.collectAsState()
    val appRowSpacingDp by viewModel.appRowSpacingDp.collectAsState()
    val clockSizeSp by viewModel.clockSizeSp.collectAsState()
    val dateSizeSp by viewModel.dateSizeSp.collectAsState()
    val waveFontSizeSp by viewModel.waveFontSizeSp.collectAsState()

    val appFontFamily = remember(appFont) { appFont.toFontFamily() }
    val clockDateFontFamily = remember(clockDateFont) { clockDateFont.toFontFamily() }
    val waveFontFamily = remember(waveFont) { waveFont.toFontFamily() }

    Surface(
        color = PureBlack,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = PureWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.typographyTitle,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            }

            // Fixed Live Preview Card above scrollable controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                SectionHeader(strings.previewHeader)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "12:45",
                                fontSize = clockSizeSp.coerceIn(28, 84).sp,
                                fontWeight = FontWeight.Light,
                                fontFamily = clockDateFontFamily,
                                color = PureWhite
                            )
                            Text(
                                text = "${strings.previewSampleDate} • 85%",
                                fontSize = dateSizeSp.coerceIn(11, 24).sp,
                                fontFamily = clockDateFontFamily,
                                color = GrayLight
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = strings.previewSampleApp,
                                fontSize = appFontSizeSp.coerceIn(13, 24).sp,
                                fontFamily = appFontFamily,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height((appRowSpacingDp.coerceIn(2, 18) * 2).dp))
                            Text(
                                text = strings.previewSampleAppSecond,
                                fontSize = appFontSizeSp.coerceIn(13, 24).sp,
                                fontFamily = appFontFamily,
                                color = PureWhite
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            listOf("★", "A", "B", "C").forEach { glyph ->
                                Text(
                                    text = glyph,
                                    fontSize = waveFontSizeSp.coerceIn(9, 16).sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = waveFontFamily,
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(top = 8.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // App Names Font & Size
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(strings.typographyAppSection)
                    FontFamilySelector(
                        label = strings.fontFamilyLabel,
                        selectedFont = appFont,
                        onSelectFont = { viewModel.setAppFont(it) }
                    )
                    TypographySliderItem(
                        title = strings.appFontSizeTitle,
                        subtitle = strings.appFontSizeSubtitle,
                        value = appFontSizeSp,
                        valueRange = 13f..24f,
                        onValueChange = { viewModel.setAppFontSizeSp(it) }
                    )
                    TypographySliderItem(
                        title = strings.appRowSpacingTitle,
                        subtitle = strings.appRowSpacingSubtitle,
                        value = appRowSpacingDp,
                        valueRange = 2f..18f,
                        unit = "dp",
                        onValueChange = { viewModel.setAppRowSpacingDp(it) }
                    )
                }

                // Clock & Date Font & Sizes
                item {
                    HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                    SectionHeader(strings.typographyClockDateSection)
                    FontFamilySelector(
                        label = strings.fontFamilyLabel,
                        selectedFont = clockDateFont,
                        onSelectFont = { viewModel.setClockDateFont(it) }
                    )
                    TypographySliderItem(
                        title = strings.clockSizeTitle,
                        subtitle = strings.clockSizeSubtitle,
                        value = clockSizeSp,
                        valueRange = 28f..84f,
                        onValueChange = { viewModel.setClockSizeSp(it) }
                    )
                    TypographySliderItem(
                        title = strings.dateSizeTitle,
                        subtitle = strings.dateSizeSubtitle,
                        value = dateSizeSp,
                        valueRange = 11f..24f,
                        onValueChange = { viewModel.setDateSizeSp(it) }
                    )
                }

                // Wave Letters Font & Size
                item {
                    HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                    SectionHeader(strings.typographyWaveSection)
                    FontFamilySelector(
                        label = strings.fontFamilyLabel,
                        selectedFont = waveFont,
                        onSelectFont = { viewModel.setWaveFont(it) }
                    )
                    TypographySliderItem(
                        title = strings.waveFontSizeTitle,
                        subtitle = strings.waveFontSizeSubtitle,
                        value = waveFontSizeSp,
                        valueRange = 9f..16f,
                        onValueChange = { viewModel.setWaveFontSizeSp(it) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun FontFamilySelector(
    label: String,
    selectedFont: LauncherFont,
    onSelectFont: (LauncherFont) -> Unit
) {
    val strings = LocalAppStrings.current
    val options = remember(strings) {
        listOf(
            LauncherFont.SYSTEM to strings.fontNameSystem,
            LauncherFont.CONDENSED to strings.fontNameCondensed,
            LauncherFont.THIN to strings.fontNameThin,
            LauncherFont.SERIF to strings.fontNameSerif,
            LauncherFont.MONOSPACE to strings.fontNameMonospace,
            LauncherFont.CURSIVE to strings.fontNameCursive
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(label, color = PureWhite, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { (fontOption, fontLabel) ->
                        val selected = selectedFont == fontOption
                        val previewFamily = remember(fontOption) { fontOption.toFontFamily() }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) PureWhite else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (selected) PureWhite else BorderDark,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectFont(fontOption) }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fontLabel,
                                color = if (selected) PureBlack else GrayLight,
                                fontSize = 13.sp,
                                fontFamily = previewFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypographySliderItem(
    title: String,
    subtitle: String,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String = "sp",
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = PureWhite,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$value $unit",
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = GrayMedium,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = PureWhite,
                activeTrackColor = PureWhite,
                inactiveTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
private fun UsageStatsSubScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val waveUsageStats by viewModel.waveUsageStats.collectAsState()
    val uiState by viewModel.launcherState.collectAsState()

    val waveSummary = when {
        waveUsageStats.totalCount == 0 -> strings.waveUsageNone
        waveUsageStats.leftCount > waveUsageStats.rightCount ->
            strings.waveUsageLeftMore(waveUsageStats.leftPercent)
        waveUsageStats.rightCount > waveUsageStats.leftCount ->
            strings.waveUsageRightMore(waveUsageStats.rightPercent)
        else -> strings.waveUsageEqual
    }

    val leftLeads = waveUsageStats.totalCount > 0 && waveUsageStats.leftCount > waveUsageStats.rightCount
    val rightLeads = waveUsageStats.totalCount > 0 && waveUsageStats.rightCount > waveUsageStats.leftCount

    Surface(
        color = PureBlack,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = PureWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.sectionUsageStats,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(strings.waveSideUsageTitle, color = PureWhite, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(waveSummary, color = GrayMedium, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (leftLeads) PureWhite else BorderDark,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(strings.sideLeft, color = GrayMedium, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${waveUsageStats.leftCount}× (${waveUsageStats.leftPercent}%)",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (rightLeads) PureWhite else BorderDark,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(strings.sideRight, color = GrayMedium, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${waveUsageStats.rightCount}× (${waveUsageStats.rightPercent}%)",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (waveUsageStats.totalCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            if (waveUsageStats.leftCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(waveUsageStats.leftCount.toFloat())
                                        .height(6.dp)
                                        .background(PureWhite)
                                )
                            }
                            if (waveUsageStats.rightCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(waveUsageStats.rightCount.toFloat())
                                        .height(6.dp)
                                        .background(GrayMedium)
                                )
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 16.dp))
                    Text(strings.mostLaunchedAppsTitle, color = PureWhite, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        strings.mostLaunchedAppsSubtitle,
                        color = GrayMedium,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (uiState.mostUsedApps.isEmpty()) {
                    item {
                        Text(
                            text = strings.noAppLaunchesRecorded,
                            color = GrayMedium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                } else {
                    items(uiState.mostUsedApps.take(8), key = { it.app.key }) { stat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val iconBitmap = stat.app.icon
                            if (iconBitmap != null) {
                                Image(
                                    bitmap = iconBitmap.asImageBitmap(),
                                    contentDescription = stat.app.label,
                                    modifier = Modifier.size(28.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stat.app.initialChar.toString(),
                                        color = PureWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stat.app.label,
                                    color = PureWhite,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = strings.launchedCountSubtitle(stat.launchCount),
                                    color = GrayMedium,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (stat.app.isFavorite) DarkSurfaceVariant else PureWhite)
                                    .border(
                                        1.dp,
                                        if (stat.app.isFavorite) BorderDark else PureWhite,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { viewModel.toggleFavorite(stat.app) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (stat.app.isFavorite) strings.favoriteBadgeActive else strings.favoriteBadgeAdd,
                                    color = if (stat.app.isFavorite) GrayLight else PureBlack,
                                    fontSize = 12.sp,
                                    fontWeight = if (stat.app.isFavorite) FontWeight.Normal else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                if (waveUsageStats.totalCount > 0 || uiState.mostUsedApps.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                .clickable { viewModel.resetUsageStats() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = strings.resetStatsButton,
                                color = GrayLight,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HiddenAppsSubScreen(
    hiddenApps: List<AppItem>,
    onUnhideApp: (AppItem) -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current

    Surface(
        color = PureBlack,
        modifier = Modifier.fillMaxSize()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = strings.back,
                    tint = PureWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.sectionHiddenApps(hiddenApps.size),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            if (hiddenApps.isEmpty()) {
                item {
                    Text(
                        text = strings.noHiddenApps,
                        color = GrayMedium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(hiddenApps, key = { it.key }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(app.label, color = PureWhite, fontSize = 15.sp)
                        IconButton(onClick = { onUnhideApp(app) }) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = strings.unhideAppContentDesc,
                                tint = PureWhite
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PureWhite, fontSize = 15.sp)
            Text(subtitle, color = GrayMedium, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureBlack,
                checkedTrackColor = PureWhite,
                uncheckedThumbColor = GrayMedium,
                uncheckedTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = GrayLight,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Text(title, color = PureWhite, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(subtitle, color = GrayMedium, fontSize = 12.sp)
    }
}
