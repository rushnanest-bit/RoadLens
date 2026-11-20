package com.roadlens.app.domain.util

import java.util.Locale

/**
 * Utility for formatting millisecond durations into standard human-readable timestamp strings.
 */
object TimeFormatter {

    /**
     * Formats a millisecond duration into "mm:ss" display format (e.g., 65000ms -> "01:05").
     * Uses [Locale.US] to avoid locale-specific numeral anomalies.
     *
     * @param timeMs Time duration in milliseconds.
     * @return Formatted timestamp string "mm:ss".
     */
    fun formatMmSs(timeMs: Long): String {
        val totalSeconds = (timeMs / 1000).coerceAtLeast(0L)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
