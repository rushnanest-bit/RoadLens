package com.roadlens.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for TimeFormatter millisecond formatting.
 * Strictly under 200 lines per project engineering rules.
 * Covers positive, negative, and boundary-condition scenarios (KPI 4.7).
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
    fun formatMmSs_subSecondDuration_returnsZeroSeconds() {
        // Less than 1 second should format to 00:00
        assertEquals("00:00", TimeFormatter.formatMmSs(500L))
        assertEquals("00:00", TimeFormatter.formatMmSs(999L))
    }

    @Test
    fun formatMmSs_exactMinuteBoundary_formatsCorrectly() {
        // Exactly 60 seconds
        assertEquals("01:00", TimeFormatter.formatMmSs(60000L))
        // Exactly 2 minutes
        assertEquals("02:00", TimeFormatter.formatMmSs(120000L))
    }

    @Test
    fun formatMmSs_fiftyNineSeconds_formatsCorrectly() {
        assertEquals("00:59", TimeFormatter.formatMmSs(59000L))
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

    @Test
    fun formatMmSs_overOneHourDuration_formatsTotalMinutes() {
        // 3661 seconds = 61 minutes, 1 second
        val formatted = TimeFormatter.formatMmSs(3661000L)
        assertEquals("61:01", formatted)
    }
}
