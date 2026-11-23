package de.fisch981.minimallauncher.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fisch981.minimallauncher.data.model.AppItem
import de.fisch981.minimallauncher.data.model.AppNotificationItem
import de.fisch981.minimallauncher.ui.theme.DarkSurfaceVariant
import de.fisch981.minimallauncher.ui.theme.GrayMedium
import de.fisch981.minimallauncher.ui.theme.GrayMuted
import de.fisch981.minimallauncher.ui.theme.PureWhite

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    app: AppItem,
    notifications: List<AppNotificationItem>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSwipeRight: () -> Unit = onLongClick,
    showIcon: Boolean = true,
    showNotificationPreview: Boolean = true,
    fontFamily: FontFamily = FontFamily.SansSerif,
    fontSizeSp: Int = 17,
    hasLeftWave: Boolean = false,
    hasRightWave: Boolean = true,
    modifier: Modifier = Modifier
) {
    val activeNotification = notifications.firstOrNull()
    val view = LocalView.current
    val thresholdPx = with(LocalDensity.current) { 48.dp.toPx() }
    val currentOnSwipeRight by rememberUpdatedState(onSwipeRight)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(app.key) {
                var totalDragX = 0f
                var triggered = false
                detectHorizontalDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        triggered = false
                    },
                    onDragEnd = {
                        totalDragX = 0f
                        triggered = false
                    },
                    onDragCancel = {
                        totalDragX = 0f
                        triggered = false
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDragX += dragAmount
                        if (!triggered && totalDragX > thresholdPx) {
                            triggered = true
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            currentOnSwipeRight()
                        }
                    }
                )
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(
                start = if (hasLeftWave) 72.dp else 28.dp,
                end = if (hasRightWave) 56.dp else 24.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon (Lines outline or Monochrome converted)
        if (showIcon) {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon.asImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier.size(34.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.label.take(1).uppercase(),
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Notification Dot
                if (notifications.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(PureWhite)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))
        }

        // App Label + Notification Snippet
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.label,
                    fontSize = fontSizeSp.coerceIn(13, 24).sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = fontFamily,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (!showIcon && notifications.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PureWhite)
                    )
                }
            }

            // Notification Preview directly under the app name
            if (showNotificationPreview && activeNotification != null) {
                val previewText = buildString {
                    if (!activeNotification.title.isNullOrBlank()) {
                        append(activeNotification.title)
                        if (!activeNotification.text.isNullOrBlank()) append(": ")
                    }
                    if (!activeNotification.text.isNullOrBlank()) {
                        append(activeNotification.text)
                    }
                }
                if (previewText.isNotBlank()) {
                    Text(
                        text = previewText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = fontFamily,
                        color = GrayMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Sub-apps / Folder indicator
        if (app.subApps.isNotEmpty()) {
            Text(
                text = "+${app.subApps.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = GrayMuted,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
