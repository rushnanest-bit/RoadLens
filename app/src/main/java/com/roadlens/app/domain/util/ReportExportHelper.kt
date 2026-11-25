package com.roadlens.app.domain.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility helper for formatting and exporting inspection CSV reports.
 * Strictly under 100 lines per project engineering rules.
 */
object ReportExportHelper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun generateCsv(sessions: List<ScanSessionEntity>, events: List<DetectionEventEntity>): String {
        val sb = StringBuilder()
        sb.append("Event ID,Session ID,Vehicle,Timestamp,Hazard Type,Confidence,Latitude,Longitude,Review Status\n")

        val sessionMap = sessions.associateBy { it.sessionId }
        for (event in events) {
            val vehicle = sessionMap[event.sessionId]?.vehicleLabel ?: "Unknown"
            val formattedTime = dateFormat.format(Date(event.capturedAt))
            val lat = event.latitude?.toString() ?: "N/A"
            val lon = event.longitude?.toString() ?: "N/A"
            sb.append("${event.eventId},${event.sessionId},\"$vehicle\",\"$formattedTime\",\"${event.className}\",${String.format(Locale.US, "%.2f", event.confidence)},$lat,$lon,${event.reviewStatus}\n")
        }
        return sb.toString()
    }

    fun shareCsvReport(context: Context, csvContent: String) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "roadlens_inspection_report_${System.currentTimeMillis()}.csv")
        file.writeText(csvContent)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "RoadLens Road Inspection Report")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Inspection Report via"))
    }
}
