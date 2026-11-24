package de.fisch981.minimallauncher.ui.components

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fisch981.minimallauncher.data.model.MediaState
import de.fisch981.minimallauncher.ui.theme.GrayLight
import de.fisch981.minimallauncher.ui.theme.GrayMedium
import de.fisch981.minimallauncher.ui.theme.LocalAppStrings
import de.fisch981.minimallauncher.ui.theme.PureWhite
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HeaderWidget(
    mediaState: MediaState,
    onMediaPlayPause: () -> Unit,
    onMediaNext: () -> Unit,
    onMediaPrevious: () -> Unit,
    onMediaOpenApp: () -> Unit = {},
    showClock: Boolean = true,
    clockSizeSp: Int = 52,
    showDate: Boolean = true,
    dateSizeSp: Int = 15,
    fontFamily: FontFamily = FontFamily.SansSerif,
    showBattery: Boolean = true,
    showMedia: Boolean = true,
    hasLeftWave: Boolean = false,
    hasRightWave: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var batteryPercent by remember { mutableIntStateOf(100) }

    LaunchedEffect(strings.isGerman, showClock, showDate, showBattery) {
        if (!showClock && !showDate && !showBattery) return@LaunchedEffect
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat(strings.dateFormatPattern, strings.dateLocale)
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

        while (true) {
            val now = Date()
            if (showClock) {
                currentTime = timeFormat.format(now)
            }
            if (showDate) {
                currentDate = dateFormat.format(now)
            }

            if (showBattery && batteryManager != null) {
                batteryPercent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            }
            delay(1000)
        }
    }

    val hasAnyHeaderContent = showClock || showDate || showBattery || (showMedia && mediaState.hasActiveMedia)
    if (!hasAnyHeaderContent) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (hasLeftWave) 72.dp else 28.dp,
                end = if (hasRightWave) 56.dp else 24.dp,
                top = 8.dp,
                bottom = 8.dp
            )
    ) {
        // Clock - click opens default clock/alarm
        if (showClock) {
            Text(
                text = currentTime,
                fontSize = clockSizeSp.coerceIn(28, 84).sp,
                fontWeight = FontWeight.Light,
                fontFamily = fontFamily,
                letterSpacing = (-1).sp,
                color = PureWhite,
                modifier = Modifier.clickable {
                    try {
                        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        }

        if (showClock && (showDate || showBattery)) {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // Date and optional Battery - click opens calendar
        if (showDate || showBattery) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            ) {
                if (showDate) {
                    Text(
                        text = currentDate,
                        fontSize = dateSizeSp.coerceIn(11, 24).sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = fontFamily,
                        color = GrayLight
                    )
                }
                if (showBattery) {
                    Text(
                        text = if (showDate) " • $batteryPercent%" else "$batteryPercent%",
                        fontSize = (dateSizeSp.coerceIn(11, 24) - 1).coerceIn(10, 23).sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = fontFamily,
                        color = GrayMedium
                    )
                }
            }
        }

        // Media Player Widget inline in header
        AnimatedVisibility(
            visible = showMedia && mediaState.hasActiveMedia,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (showClock || showDate || showBattery) 16.dp else 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !mediaState.packageName.isNullOrBlank()) {
                            onMediaOpenApp()
                        }
                ) {
                    Text(
                        text = mediaState.title ?: strings.unknownTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = fontFamily,
                        color = PureWhite,
                        maxLines = 1
                    )
                    if (!mediaState.artist.isNullOrBlank()) {
                        Text(
                            text = mediaState.artist,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = fontFamily,
                            color = GrayMedium,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMediaPrevious, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = strings.mediaPrevious,
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onMediaPlayPause, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (mediaState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = strings.mediaPlayPause,
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onMediaNext, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = strings.mediaNext,
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
