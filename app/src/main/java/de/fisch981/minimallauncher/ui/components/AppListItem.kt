package de.fisch981.minimallauncher.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
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
import de.fisch981.minimallauncher.ui.theme.PureBlack
import de.fisch981.minimallauncher.ui.theme.PureWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    verticalPaddingDp: Int = 8,
    hasLeftWave: Boolean = false,
    hasRightWave: Boolean = true,
    onDismissNotifications: (() -> Unit)? = null,
    slideOutOnDismiss: Boolean = false,
    modifier: Modifier = Modifier
) {
    val activeNotification = notifications.firstOrNull()
    val canDismissNotifications = onDismissNotifications != null && notifications.isNotEmpty()
    val view = LocalView.current
    val density = LocalDensity.current
    val popupSwipeThresholdPx = with(density) { 48.dp.toPx() }
    val dismissThresholdPx = with(density) { 88.dp.toPx() }
    val currentOnSwipeRight by rememberUpdatedState(onSwipeRight)
    val currentOnDismissNotifications by rememberUpdatedState(onDismissNotifications)
    val currentCanDismissNotifications by rememberUpdatedState(canDismissNotifications)
    val currentSlideOutOnDismiss by rememberUpdatedState(slideOutOnDismiss)
    val swipeOffsetX = remember(app.key) { Animatable(0f) }
    var rowWidthPx by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val vPad = verticalPaddingDp.coerceIn(2, 18).dp
    val startInset = if (hasLeftWave) 72.dp else 28.dp
    val endInset = if (hasRightWave) 56.dp else 24.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { rowWidthPx = it.width }
    ) {
        val currentSwipeOffset = swipeOffsetX.value
        if (abs(currentSwipeOffset) > 4f) {
            val dragProgress = (abs(currentSwipeOffset) / dismissThresholdPx).coerceIn(0f, 1f)
            val thresholdReached = abs(currentSwipeOffset) >= dismissThresholdPx
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(PureBlack)
                    .padding(start = startInset, end = endInset),
                contentAlignment = if (currentSwipeOffset > 0f) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = if (thresholdReached) PureWhite else GrayMedium,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            val iconScale = 0.85f + 0.20f * dragProgress
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val offset = swipeOffsetX.value
                    translationX = offset
                    if (abs(offset) > 0f) {
                        val progress = (abs(offset) / dismissThresholdPx).coerceIn(0f, 1.5f)
                        alpha = if (currentSlideOutOnDismiss) {
                            (1f - progress * 0.55f).coerceIn(0.15f, 1f)
                        } else {
                            (1f - progress * 0.30f).coerceIn(0.55f, 1f)
                        }
                    }
                }
                .background(PureBlack)
                .pointerInput(app.key) {
                    var totalDragX = 0f
                    var popupTriggered = false
                    var dismissDragActive = false
                    var thresholdHapticFired = false

                    detectHorizontalDragGestures(
                        onDragStart = {
                            dismissDragActive = currentCanDismissNotifications
                            totalDragX = if (dismissDragActive) swipeOffsetX.value else 0f
                            popupTriggered = false
                            thresholdHapticFired = abs(totalDragX) >= dismissThresholdPx
                            if (dismissDragActive) {
                                coroutineScope.launch { swipeOffsetX.stop() }
                            }
                        },
                        onDragEnd = {
                            if (dismissDragActive) {
                                val finalOffset = totalDragX
                                if (abs(finalOffset) >= dismissThresholdPx) {
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    if (currentSlideOutOnDismiss) {
                                        val exitTarget = if (finalOffset > 0f) {
                                            rowWidthPx.coerceAtLeast(300).toFloat()
                                        } else {
                                            -rowWidthPx.coerceAtLeast(300).toFloat()
                                        }
                                        coroutineScope.launch {
                                            swipeOffsetX.animateTo(
                                                targetValue = exitTarget,
                                                animationSpec = tween(durationMillis = 150)
                                            )
                                            currentOnDismissNotifications?.invoke()
                                            delay(150L)
                                            swipeOffsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                            )
                                        }
                                    } else {
                                        currentOnDismissNotifications?.invoke()
                                        coroutineScope.launch {
                                            swipeOffsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                            )
                                        }
                                    }
                                } else {
                                    coroutineScope.launch {
                                        swipeOffsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                        )
                                    }
                                }
                            }
                            totalDragX = 0f
                            popupTriggered = false
                            dismissDragActive = false
                            thresholdHapticFired = false
                        },
                        onDragCancel = {
                            if (dismissDragActive) {
                                coroutineScope.launch {
                                    swipeOffsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                }
                            }
                            totalDragX = 0f
                            popupTriggered = false
                            dismissDragActive = false
                            thresholdHapticFired = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            totalDragX += dragAmount
                            if (dismissDragActive) {
                                val visualOffset = if (currentSlideOutOnDismiss) {
                                    totalDragX
                                } else {
                                    val maxOffset = dismissThresholdPx * 1.45f
                                    totalDragX.coerceIn(-maxOffset, maxOffset)
                                }
                                totalDragX = visualOffset
                                coroutineScope.launch {
                                    swipeOffsetX.snapTo(visualOffset)
                                }
                                val crossed = abs(visualOffset) >= dismissThresholdPx
                                if (crossed && !thresholdHapticFired) {
                                    thresholdHapticFired = true
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                } else if (!crossed) {
                                    thresholdHapticFired = false
                                }
                            } else {
                                if (!popupTriggered && totalDragX > popupSwipeThresholdPx) {
                                    popupTriggered = true
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    currentOnSwipeRight()
                                }
                            }
                        }
                    )
                }
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(
                    start = startInset,
                    end = endInset,
                    top = vPad,
                    bottom = vPad
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
}
