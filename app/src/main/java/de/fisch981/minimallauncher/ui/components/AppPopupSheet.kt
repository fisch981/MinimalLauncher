package de.fisch981.minimallauncher.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fisch981.minimallauncher.data.model.AppItem
import de.fisch981.minimallauncher.data.model.AppNotificationItem
import de.fisch981.minimallauncher.data.model.ShortcutItem
import de.fisch981.minimallauncher.ui.theme.BorderDark
import de.fisch981.minimallauncher.ui.theme.DarkSurface
import de.fisch981.minimallauncher.ui.theme.GrayLight
import de.fisch981.minimallauncher.ui.theme.GrayMedium
import de.fisch981.minimallauncher.ui.theme.GrayMuted
import de.fisch981.minimallauncher.ui.theme.LocalAppStrings
import de.fisch981.minimallauncher.ui.theme.PureBlack
import de.fisch981.minimallauncher.ui.theme.PureWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppPopupSheet(
    app: AppItem,
    shortcuts: List<ShortcutItem>,
    notifications: List<AppNotificationItem>,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRenameAppClick: () -> Unit = {},
    onEditIconClick: () -> Unit,
    onResetIconClick: () -> Unit = {},
    onHideApp: () -> Unit,
    onLaunchShortcut: (ShortcutItem) -> Unit,
    onLaunchSubApp: (AppItem) -> Unit,
    onAddSubAppClick: () -> Unit,
    onNotificationActionClick: (notificationKey: String, actionIndex: Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val view = LocalView.current
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .background(GrayMuted, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: App Icon and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon.asImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = app.label,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 11.sp,
                        color = GrayMedium
                    )
                }
            }

            // Pop-up Folder / Sub-Apps
            if (app.subApps.isNotEmpty()) {
                Text(
                    text = strings.folderSubAppsHeader,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                app.subApps.forEach { subApp ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLaunchSubApp(subApp)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (subApp.icon != null) {
                            Image(
                                bitmap = subApp.icon.asImageBitmap(),
                                contentDescription = subApp.label,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = subApp.label,
                            fontSize = 15.sp,
                            color = PureWhite
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // App Shortcuts
            if (shortcuts.isNotEmpty()) {
                Text(
                    text = strings.shortcutsHeader,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                shortcuts.forEach { shortcut ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLaunchShortcut(shortcut)
                                onDismiss()
                            }
                            .padding(vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (shortcut.icon != null) {
                            Image(
                                bitmap = shortcut.icon.asImageBitmap(),
                                contentDescription = shortcut.shortLabel,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = GrayLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = shortcut.shortLabel,
                            fontSize = 15.sp,
                            color = PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Active Notifications
            if (notifications.isNotEmpty()) {
                Text(
                    text = strings.notificationsHeader,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                notifications.forEach { notif ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            if (!notif.title.isNullOrBlank()) {
                                Text(
                                    text = notif.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PureWhite
                                )
                            }
                            if (!notif.text.isNullOrBlank()) {
                                Text(
                                    text = notif.text,
                                    fontSize = 13.sp,
                                    color = GrayLight
                                )
                            }
                            if (notif.actions.isNotEmpty()) {
                                FlowRow(
                                    modifier = Modifier.padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    notif.actions.forEach { action ->
                                        Text(
                                            text = action.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = GrayLight,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .border(0.75.dp, BorderDark, RoundedCornerShape(4.dp))
                                                .clickable {
                                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                    onNotificationActionClick(notif.key, action.index)
                                                    onDismiss()
                                                }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 8.dp))

            // Actions: Favorite, Rename, Change icon, Restore icon, Add sub-app, Hide, App-Info
            PopupActionItem(
                icon = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                label = if (isFavorite) strings.removeFromFavorites else strings.addToFavorites,
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            PopupActionItem(
                icon = Icons.Default.Edit,
                label = strings.renameAppAction,
                onClick = {
                    onRenameAppClick()
                    onDismiss()
                }
            )

            PopupActionItem(
                icon = Icons.Default.Edit,
                label = strings.changeIconAction,
                onClick = {
                    onEditIconClick()
                    onDismiss()
                }
            )

            PopupActionItem(
                icon = Icons.Default.Refresh,
                label = strings.restoreDefaultIconAction,
                onClick = {
                    onResetIconClick()
                    onDismiss()
                }
            )

            PopupActionItem(
                icon = Icons.Default.Add,
                label = strings.addSubAppToFolderAction,
                onClick = {
                    onAddSubAppClick()
                    onDismiss()
                }
            )

            PopupActionItem(
                icon = Icons.Default.VisibilityOff,
                label = strings.hideAppAction,
                onClick = {
                    onHideApp()
                    onDismiss()
                }
            )

            PopupActionItem(
                icon = Icons.Default.Info,
                label = strings.systemAppDetailsAction,
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", app.packageName, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun PopupActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PureWhite,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = PureWhite
        )
    }
}
