package de.fisch981.minimallauncher.ui.components

import android.app.ActivityOptions
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.fisch981.minimallauncher.data.model.MediaState
import de.fisch981.minimallauncher.ui.theme.GrayLight
import de.fisch981.minimallauncher.ui.theme.GrayMedium
import de.fisch981.minimallauncher.ui.theme.LocalAppStrings
import de.fisch981.minimallauncher.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    showNextAlarm: Boolean = true,
    showMedia: Boolean = true,
    hasLeftWave: Boolean = false,
    hasRightWave: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val strings = LocalAppStrings.current
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var batteryPercent by remember { mutableIntStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }
    var nextAlarmText by remember { mutableStateOf<String?>(null) }
    var nextAlarmShowIntent by remember { mutableStateOf<PendingIntent?>(null) }

    DisposableEffect(
        context,
        lifecycleOwner,
        strings.isGerman,
        showClock,
        showDate,
        showBattery,
        showNextAlarm
    ) {
        if (!showClock && !showDate && !showBattery && !showNextAlarm) {
            return@DisposableEffect onDispose {}
        }

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat(strings.dateFormatPattern, strings.dateLocale)

        fun updateFormatterTimeZones() {
            val tz = TimeZone.getDefault()
            timeFormat.timeZone = tz
            dateFormat.timeZone = tz
        }

        fun refreshTimeAndDate(now: Date = Date()) {
            if (showClock) {
                currentTime = timeFormat.format(now)
            }
            if (showDate) {
                currentDate = dateFormat.format(now)
            }
        }

        fun refreshBattery(batteryIntent: Intent? = null) {
            if (!showBattery) return
            val managerFallback = batteryManager
                ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                ?.takeIf { it in 0..100 }
                ?: batteryPercent
            batteryPercent = HeaderFormatUtils.parseBatteryPercent(batteryIntent, managerFallback)
            val chargingFallback = batteryManager?.isCharging ?: isCharging
            isCharging = HeaderFormatUtils.parseIsCharging(batteryIntent, chargingFallback)
        }

        fun refreshNextAlarm(nowMillis: Long = System.currentTimeMillis()) {
            if (showNextAlarm) {
                val alarmInfo = try {
                    alarmManager?.nextAlarmClock
                } catch (_: Exception) {
                    null
                }
                if (alarmInfo != null) {
                    val formatted = HeaderFormatUtils.formatNextAlarm(
                        triggerTimeMillis = alarmInfo.triggerTime,
                        nowMillis = nowMillis,
                        locale = strings.dateLocale
                    )
                    nextAlarmText = formatted
                    nextAlarmShowIntent = if (formatted != null) alarmInfo.showIntent else null
                } else {
                    nextAlarmText = null
                    nextAlarmShowIntent = null
                }
            } else {
                nextAlarmText = null
                nextAlarmShowIntent = null
            }
        }

        fun refreshAll(batteryIntent: Intent? = null) {
            val now = Date()
            refreshTimeAndDate(now)
            refreshBattery(batteryIntent)
            refreshNextAlarm(now.time)
        }

        val filter = IntentFilter().apply {
            if (showClock || showDate || showNextAlarm) {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_LOCALE_CHANGED)
            }
            if (showBattery) {
                addAction(Intent.ACTION_BATTERY_CHANGED)
            }
            if (showNextAlarm) {
                addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)
            }
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BATTERY_CHANGED -> refreshBattery(intent)
                    AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED -> refreshNextAlarm()
                    Intent.ACTION_TIMEZONE_CHANGED,
                    Intent.ACTION_LOCALE_CHANGED -> {
                        updateFormatterTimeZones()
                        refreshAll()
                    }
                    Intent.ACTION_TIME_TICK,
                    Intent.ACTION_TIME_CHANGED -> {
                        val now = Date()
                        refreshTimeAndDate(now)
                        if (showNextAlarm && nextAlarmText != null) {
                            refreshNextAlarm(now.time)
                        }
                    }
                    else -> refreshAll()
                }
            }
        }

        val initialSticky = runCatching {
            ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        }.getOrNull()
        refreshAll(initialSticky)

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                updateFormatterTimeZones()
                refreshAll()
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            runCatching { lifecycleOwner.lifecycle.removeObserver(lifecycleObserver) }
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    val clampedDateSize = dateSizeSp.coerceIn(11, 24)
    val hasActiveAlarm = showNextAlarm && !nextAlarmText.isNullOrBlank()
    val hasAnyHeaderContent =
        showClock || showDate || showBattery || hasActiveAlarm || (showMedia && mediaState.hasActiveMedia)
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
                    launchAlarmApp(context, null)
                }
            )
        }

        if (showClock && (showDate || showBattery || hasActiveAlarm)) {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // Date, optional Battery, and strict monochrome Next Alarm
        if (showDate || showBattery || hasActiveAlarm) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                fontSize = clampedDateSize.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = fontFamily,
                                color = GrayLight
                            )
                        }
                        if (showBattery) {
                            Text(
                                text = if (showDate) " • $batteryPercent%" else "$batteryPercent%",
                                fontSize = clampedDateSize.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = fontFamily,
                                color = GrayLight
                            )
                            if (isCharging) {
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = strings.batteryChargingContentDesc,
                                    tint = GrayLight,
                                    modifier = Modifier.size((clampedDateSize - 1).coerceIn(11, 22).dp)
                                )
                            }
                        }
                    }
                }

                if (hasActiveAlarm) {
                    if (showDate || showBattery) {
                        Text(
                            text = " • ",
                            fontSize = clampedDateSize.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = fontFamily,
                            color = GrayLight
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            launchAlarmApp(context, nextAlarmShowIntent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Alarm,
                            contentDescription = strings.nextAlarmContentDesc,
                            tint = GrayLight,
                            modifier = Modifier.size((clampedDateSize - 1).coerceIn(11, 22).dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = nextAlarmText.orEmpty(),
                            fontSize = clampedDateSize.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = fontFamily,
                            color = GrayLight
                        )
                    }
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
                    .padding(top = if (showClock || showDate || showBattery || hasActiveAlarm) 16.dp else 4.dp),
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

private fun launchAlarmApp(context: Context, showIntent: PendingIntent?) {
    if (showIntent != null) {
        val launched = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic()
                    .setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                    .toBundle()
                showIntent.send(context, 0, null, null, null, null, options)
            } else {
                showIntent.send()
            }
            true
        } catch (_: Exception) {
            false
        }
        if (launched) return
    }
    try {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
