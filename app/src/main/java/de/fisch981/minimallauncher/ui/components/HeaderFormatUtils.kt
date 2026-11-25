package de.fisch981.minimallauncher.ui.components

import android.content.Intent
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

internal object HeaderFormatUtils {

    fun formatNextAlarm(triggerTimeMillis: Long, nowMillis: Long, locale: Locale): String? {
        if (triggerTimeMillis <= 0L || triggerTimeMillis < nowMillis - 60_000L) {
            return null
        }
        val deltaMs = triggerTimeMillis - nowMillis
        val date = Date(triggerTimeMillis)
        val pattern = if (deltaMs < 24L * 60L * 60L * 1000L) "HH:mm" else "EEE HH:mm"
        val formatted = SimpleDateFormat(pattern, locale).format(date)
        return formatted.replace("..", ".").replace(Regex("\\s+"), " ").trim()
    }

    fun parseBatteryPercent(intent: Intent?, fallbackPercent: Int): Int {
        if (intent != null) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            return computeBatteryPercent(level, scale, fallbackPercent)
        }
        return fallbackPercent.coerceIn(0, 100)
    }

    fun computeBatteryPercent(level: Int, scale: Int, fallbackPercent: Int): Int {
        if (level >= 0 && scale > 0) {
            return ((level * 100f) / scale).roundToInt().coerceIn(0, 100)
        }
        return fallbackPercent.coerceIn(0, 100)
    }

    fun parseIsCharging(intent: Intent?, fallbackIsCharging: Boolean): Boolean {
        if (intent != null) {
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            return computeIsCharging(status, fallbackIsCharging)
        }
        return fallbackIsCharging
    }

    fun computeIsCharging(status: Int, fallbackIsCharging: Boolean): Boolean {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> true
            BatteryManager.BATTERY_STATUS_DISCHARGING,
            BatteryManager.BATTERY_STATUS_NOT_CHARGING,
            BatteryManager.BATTERY_STATUS_FULL -> false
            else -> fallbackIsCharging
        }
    }
}
