package com.roadlens.app.domain.model

/**
 * Represents the active media input mode for road scanning.
 * Strictly under 200 lines per project engineering rules.
 *
 * @property title Human-readable label for UI tabs and buttons.
 * @property icon Unicode or display symbol representing the input mode.
 */
enum class ScanInputMode(
    val title: String,
    val icon: String
) {
    CAMERA(title = "Live Camera", icon = "📷"),
    IMAGE(title = "Image", icon = "🖼"),
    VIDEO(title = "Video Replay", icon = "▶")
}
