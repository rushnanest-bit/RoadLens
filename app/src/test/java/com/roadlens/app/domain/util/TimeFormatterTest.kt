package com.roadlens.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for TimeFormatter millisecond formatting.
 * Kept below 200 lines per project engineering rules.
 */
class TimeFormatterTest {

    @Test
    fun formatMmSs_zeroMilliseconds_returnsDoubleZero() {
        val formatted = TimeFormatter.formatMmSs(0L)
        assertEquals("00:00", formatted)
    }

    @Test
    fun formatMmSs_negativeMilliseconds_coercedToZero() {
        val formatted = TimeFormatter.formatMmSs(-5000L)
        assertEquals("00:00", formatted)
    }

    @Test
    fun formatMmSs_sixtyFiveSeconds_returnsOneMinuteFiveSeconds() {
        val formatted = TimeFormatter.formatMmSs(65000L)
        assertEquals("01:05", formatted)
    }

    @Test
    fun formatMmSs_largeDuration_formatsCorrectly() {
        // 125 seconds = 2 minutes, 5 seconds
        val formatted = TimeFormatter.formatMmSs(125000L)
        assertEquals("02:05", formatted)
    }
}
