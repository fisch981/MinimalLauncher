package de.fisch981.minimallauncher

import android.os.BatteryManager
import de.fisch981.minimallauncher.ui.components.HeaderFormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HeaderFormatUtilsTest {

    @Test
    fun formatNextAlarm_returnsTimeWithin24HoursAndWeekdayBeyond24Hours() {
        val baseNow = 1_795_530_000_000L
        val inThreeHours = baseNow + 3L * 60L * 60L * 1000L
        val expectedShort = SimpleDateFormat("HH:mm", Locale.GERMAN).format(Date(inThreeHours))
        assertEquals(expectedShort, HeaderFormatUtils.formatNextAlarm(inThreeHours, baseNow, Locale.GERMAN))

        val inThirtyHours = baseNow + 30L * 60L * 60L * 1000L
        val formattedFuture = HeaderFormatUtils.formatNextAlarm(inThirtyHours, baseNow, Locale.GERMAN)
        assertTrue(!formattedFuture.isNullOrBlank())
        assertTrue(formattedFuture!!.contains(SimpleDateFormat("HH:mm", Locale.GERMAN).format(Date(inThirtyHours))))
    }

    @Test
    fun formatNextAlarm_returnsNullForUnsetOrPastTriggers() {
        val now = 1_795_530_000_000L
        assertNull(HeaderFormatUtils.formatNextAlarm(0L, now, Locale.GERMAN))
        assertNull(HeaderFormatUtils.formatNextAlarm(now - 120_000L, now, Locale.GERMAN))
    }

    @Test
    fun computeBatteryPercent_handlesValidAndFallbackValues() {
        assertEquals(75, HeaderFormatUtils.computeBatteryPercent(level = 75, scale = 100, fallbackPercent = 50))
        assertEquals(50, HeaderFormatUtils.computeBatteryPercent(level = 1, scale = 2, fallbackPercent = 10))
        assertEquals(42, HeaderFormatUtils.parseBatteryPercent(null, fallbackPercent = 42))
        assertEquals(100, HeaderFormatUtils.computeBatteryPercent(level = -1, scale = 0, fallbackPercent = 130))
    }

    @Test
    fun computeIsCharging_handlesChargingDischargingAndFallback() {
        assertTrue(HeaderFormatUtils.computeIsCharging(BatteryManager.BATTERY_STATUS_CHARGING, fallbackIsCharging = false))
        assertFalse(HeaderFormatUtils.computeIsCharging(BatteryManager.BATTERY_STATUS_DISCHARGING, fallbackIsCharging = true))
        assertFalse(HeaderFormatUtils.computeIsCharging(BatteryManager.BATTERY_STATUS_NOT_CHARGING, fallbackIsCharging = true))
        assertFalse(HeaderFormatUtils.computeIsCharging(BatteryManager.BATTERY_STATUS_FULL, fallbackIsCharging = true))
        assertTrue(HeaderFormatUtils.computeIsCharging(-1, fallbackIsCharging = true))
        assertFalse(HeaderFormatUtils.parseIsCharging(null, fallbackIsCharging = false))
        assertTrue(HeaderFormatUtils.parseIsCharging(null, fallbackIsCharging = true))
    }
}
