package com.roadlens.app.domain.model

/**
 * Defect type filters for road hazards on the map.
 * Strictly under 200 lines per project engineering rules.
 */
enum class MapDefectFilter(val title: String) {
    ALL("All Hazards"),
    POTHOLES("Potholes"),
    CRACKS_ALL("All Cracks"),
    ALLIGATOR("Alligator (D20)"),
    LONGITUDINAL("Longitudinal (D00)"),
    TRANSVERSE("Transverse (D10)")
}

/**
 * Verification workflow status filters for road hazards on the map.
 */
enum class MapStatusFilter(val title: String) {
    ALL("All"),
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    DISMISSED("Dismissed")
}

typealias MapFilterType = MapDefectFilter
