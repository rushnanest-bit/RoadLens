# RoadLens – UML Architecture Diagrams (KPI 2.1)

This document contains the official UML Use-Case and Sequence Diagrams representing the implemented functionality of RoadLens, adhering strictly to KPI 2.1.

---

## 1. UML Use-Case Diagram

The use-case diagram illustrates the interactions between primary actors (Fleet Driver / Operator, Fleet Supervisor / Reviewer, System Clock / Hardware Sensors) and the RoadLens application.

```mermaid
graph TD
    subgraph Actors
        Driver["Fleet Driver / Field Operator"]
        Supervisor["Fleet Supervisor / Reviewer"]
        Sensors["Hardware Sensors (Camera/GPS)"]
    end

    subgraph RoadLens Application
        UC1["UC-01: Select Inspection Mode (Image / Video)"]
        UC2["UC-02: Import Road Surface Media"]
        UC3["UC-03: Execute On-Device YOLO Inference"]
        UC4["UC-04: Filter Distresses (Threshold >= 0.25)"]
        UC5["UC-05: Suppress Overlapping Duplicates (NMS)"]
        UC6["UC-06: Deduplicate Temporal Video Frames"]
        UC7["UC-07: Display Real-Time Canvas Overlay"]
        UC8["UC-08: Review Defect Timeline & Timeline Scrubbing"]
        UC9["UC-09: Inspect Dashboard Fleet Metrics"]
    end

    Driver --> UC1
    Driver --> UC2
    Driver --> UC7
    Driver --> UC8

    Supervisor --> UC8
    Supervisor --> UC9

    Sensors --> UC3
    UC2 --> UC3
    UC3 --> UC4
    UC4 --> UC5
    UC5 --> UC6
    UC6 --> UC7
    UC7 --> UC8
    UC6 --> UC9
```

---

## 2. UML Sequence Diagram: Video Defect Detection & Deduplication Pipeline

This sequence diagram depicts the end-to-end execution flow of real-time road distress perception from video playback to state update and UI overlay.

```mermaid
sequenceDiagram
    autonumber
    actor User as User / Driver
    participant View as ScanScreen (Compose UI)
    participant Canvas as LiveVideoDetectorCanvas
    participant VM as ScanViewModel
    participant Detector as RoadDefectDetector (Interface)
    participant Onnx as OnnxYoloDetector
    participant Pre as YoloPreprocessor
    participant Session as OrtSession (ONNX Runtime)
    participant Post as YoloPostprocessor
    participant Geom as GeometryUtils

    User->>View: Selects Road Video
    View->>VM: selectVideo(uri)
    VM-->>View: State updated (inputMode = VIDEO, isPlaying = true)

    View->>Canvas: Renders Surface & TextureView
    loop Periodic Frame Sampling (Every 100ms)
        Canvas->>Canvas: Capture TextureView bitmap (480x270)
        Canvas->>Detector: detectOnly(bitmap)
        Detector->>Onnx: detectOnly(bitmap)
        Onnx->>Pre: preprocessZeroAlloc(bitmap)
        Pre-->>Onnx: Normalized NCHW float array [1, 3, 640, 640]
        Onnx->>Session: run(inputs)
        Session-->>Onnx: Raw predictions [1, 8, 8400]
        Onnx->>Post: processPredictions(predictions, width, height)
        Post->>Geom: calculateIoU() for NMS suppression
        Geom-->>Post: Filtered non-overlapping boxes
        Post-->>Onnx: List<RoadDetection>
        Onnx-->>Canvas: List<RoadDetection>
        Canvas->>Canvas: Draw bounding boxes and confidence on native Canvas
        Canvas->>VM: processAnalyzedFrame(detections, frameTimeMs)
        VM->>Geom: calculateIoU / areCentersClose (Temporal Deduplication)
        Geom-->>VM: Match with existing defects within 1200ms
        VM->>VM: Update defect timeline & cumulative counts
        VM-->>View: Emit updated ScanScreenUiState via StateFlow
        View->>User: Recomposes UI (Timeline, counters, status)
    end
```
