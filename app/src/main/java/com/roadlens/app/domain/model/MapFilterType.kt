package com.roadlens.app.domain.model

/**
 * Filter categories for plotting and inspecting road hazards on the map.
 * Strictly under 200 lines per project engineering rules.
 */
enum class MapFilterType(val title: String) {
    ALL("All Hazards"),
    POTHOLES("Potholes (D40)"),
    CRACKS("Cracks (D00-D20)"),
    PENDING("Pending Review")
}
