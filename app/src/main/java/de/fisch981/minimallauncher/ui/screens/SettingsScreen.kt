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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fisch981.minimallauncher.data.model.ScrollerSide
import de.fisch981.minimallauncher.data.repository.LauncherUtils
import de.fisch981.minimallauncher.data.service.LauncherNotificationService
import de.fisch981.minimallauncher.ui.theme.AppLanguage
import de.fisch981.minimallauncher.ui.theme.BorderDark
import de.fisch981.minimallauncher.ui.theme.DarkSurfaceVariant
import de.fisch981.minimallauncher.ui.theme.GrayLight
import de.fisch981.minimallauncher.ui.theme.GrayMedium
import de.fisch981.minimallauncher.ui.theme.LocalAppStrings
import de.fisch981.minimallauncher.ui.theme.PureBlack
import de.fisch981.minimallauncher.ui.theme.PureWhite
import de.fisch981.minimallauncher.ui.viewmodel.LauncherViewModel
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

    val appLanguage by viewModel.appLanguage.collectAsState()
    val scrollerSide by viewModel.scrollerSide.collectAsState()
    val waveIdleAlphaPercent by viewModel.waveIdleAlphaPercent.collectAsState()
    val showBattery by viewModel.showBattery.collectAsState()
    val showMedia by viewModel.showMedia.collectAsState()
    val showRecentApps by viewModel.showRecentApps.collectAsState()
    val waveUsageStats by viewModel.waveUsageStats.collectAsState()
    val uiState by viewModel.launcherState.collectAsState()
    val customIconsCount by viewModel.customIconsCountState.collectAsState()
    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()

    val isNotificationGranted = LauncherNotificationService.isPermissionGranted(context)
    val isLinesInstalled = viewModel.iconPackManager.isLinesPackInstalled()

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

    Column(
        modifier = modifier
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
            // Standard-Launcher Card
            item {
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
                            onClick = {
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
            }

            // Backup & Restore (MinimalLauncher JSON)
            item {
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                SectionHeader(strings.sectionBackupRestore)

                SettingItem(
                    title = strings.exportBackupTitle,
                    subtitle = strings.exportBackupSubtitle,
                    onClick = {
                        try {
                            val dateStamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            exportBackupLauncher.launch("minimallauncher-backup-$dateStamp.json")
                        } catch (_: Exception) {}
                    }
                )

                SettingItem(
                    title = strings.importBackupTitle,
                    subtitle = strings.importBackupSubtitle,
                    onClick = {
                        try {
                            importBackupLauncher.launch(arrayOf("application/json", "*/*"))
                        } catch (_: Exception) {}
                    }
                )
            }

            // Nova Backup Import
            item {
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                SectionHeader(strings.sectionNovaImporter)

                SettingItem(
                    title = strings.selectNovaBackupTitle,
                    subtitle = if (customIconsCount > 0) {
                        strings.novaCustomMappingsActive(customIconsCount)
                    } else {
                        strings.novaBackupSubtitleDefault
                    },
                    onClick = {
                        try {
                            filePickerLauncher.launch(arrayOf("*/*"))
                        } catch (_: Exception) {}
                    }
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
            }

            // Favorites
            item {
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                SectionHeader(strings.sectionFavorites)

                SettingItem(
                    title = strings.manageFavoritesTitle,
                    subtitle = strings.manageFavoritesSubtitle(uiState.favoriteApps.size),
                    onClick = { viewModel.setManageFavoritesOpen(true) }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.showRecentAppsTitle, color = PureWhite, fontSize = 15.sp)
                        Text(
                            strings.showRecentAppsSubtitle,
                            color = GrayMedium,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = showRecentApps,
                        onCheckedChange = { viewModel.toggleShowRecentApps(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureBlack,
                            checkedTrackColor = PureWhite,
                            uncheckedThumbColor = GrayMedium,
                            uncheckedTrackColor = DarkSurfaceVariant
                        )
                    )
                }
            }

            // Appearance & Wave Scroller
            item {
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                SectionHeader(strings.sectionAppearanceWave)

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

                SettingItem(
                    title = strings.linesPackStatusTitle,
                    subtitle = if (isLinesInstalled) strings.linesPackActiveSubtitle else strings.linesPackInactiveSubtitle,
                    onClick = { viewModel.reloadIcons() }
                )
            }

            // Usage Statistics
            item {
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                SectionHeader(strings.sectionUsageStats)

                val waveSummary = when {
                    waveUsageStats.totalCount == 0 -> strings.waveUsageNone
                    waveUsageStats.leftCount > waveUsageStats.rightCount ->
                        strings.waveUsageLeftMore(waveUsageStats.leftPercent)
                    waveUsageStats.rightCount > waveUsageStats.leftCount ->
                        strings.waveUsageRightMore(waveUsageStats.rightPercent)
                    else -> strings.waveUsageEqual
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(strings.waveSideUsageTitle, color = PureWhite, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(waveSummary, color = GrayMedium, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val leftLeads = waveUsageStats.totalCount > 0 && waveUsageStats.leftCount > waveUsageStats.rightCount
                    val rightLeads = waveUsageStats.totalCount > 0 && waveUsageStats.rightCount > waveUsageStats.leftCount

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

                Spacer(modifier = Modifier.height(8.dp))

                Text(strings.mostLaunchedAppsTitle, color = PureWhite, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    strings.mostLaunchedAppsSubtitle,
                    color = GrayMedium,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.mostUsedApps.isEmpty()) {
                    Text(
                        text = strings.noAppLaunchesRecorded,
                        color = GrayMedium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    uiState.mostUsedApps.take(8).forEach { stat ->
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
                    Spacer(modifier = Modifier.height(10.dp))
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
                }
            }

            // Widgets & Notifications
            item {
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                SectionHeader(strings.sectionWidgetsNotifications)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.showBatteryTitle, color = PureWhite, fontSize = 15.sp)
                        Text(strings.showBatterySubtitle, color = GrayMedium, fontSize = 12.sp)
                    }
                    Switch(
                        checked = showBattery,
                        onCheckedChange = { viewModel.toggleShowBattery(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureBlack,
                            checkedTrackColor = PureWhite,
                            uncheckedThumbColor = GrayMedium,
                            uncheckedTrackColor = DarkSurfaceVariant
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.showMediaTitle, color = PureWhite, fontSize = 15.sp)
                        Text(strings.showMediaSubtitle, color = GrayMedium, fontSize = 12.sp)
                    }
                    Switch(
                        checked = showMedia,
                        onCheckedChange = { viewModel.toggleShowMedia(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureBlack,
                            checkedTrackColor = PureWhite,
                            uncheckedThumbColor = GrayMedium,
                            uncheckedTrackColor = DarkSurfaceVariant
                        )
                    )
                }

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
            }

            // Hidden Apps
            item {
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                SectionHeader(strings.sectionHiddenApps(uiState.hiddenApps.size))
            }

            if (uiState.hiddenApps.isEmpty()) {
                item {
                    Text(
                        text = strings.noHiddenApps,
                        color = GrayMedium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(uiState.hiddenApps, key = { it.key }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(app.label, color = PureWhite, fontSize = 15.sp)
                        IconButton(onClick = { viewModel.unhideApp(app) }) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = strings.unhideAppContentDesc,
                                tint = PureWhite
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "MinimalLauncher v1.0.23",
                    color = GrayMedium,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
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
