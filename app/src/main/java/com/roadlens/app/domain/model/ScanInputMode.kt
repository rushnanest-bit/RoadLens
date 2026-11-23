package com.roadlens.app.domain.model

/**
 * Top-level operational modes for the RoadLens Scan interface.
 * Distinguishes between production vehicle road scanning and test benchmarks per Project Spec 7.3.
 * Strictly under 200 lines per project engineering rules.
 */
enum class ScanOperationalMode(
    val title: String
) {
    LIVE_SCAN(title = "Live Fleet Scan"),
    DATASET_LAB(title = "Dataset Lab")
}

/**
 * Benchmark media types available within the Dataset Lab evaluation section.
 */
enum class DatasetLabSubMode(
    val title: String
) {
    IMAGE(title = "Static Image"),
    VIDEO(title = "Video Replay")
}
