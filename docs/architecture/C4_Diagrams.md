# RoadLens – C4 Architecture Models (KPI 2.2)

This document specifies the C4 Level 1 (System Context) and Level 2 (Container) architectural models for RoadLens.

---

## 1. C4 Level 1: System Context Diagram

The System Context diagram describes RoadLens in relation to its users, physical vehicle hardware, and external environments. RoadLens is designed as a **local-first, privacy-preserving on-device intelligence system** that functions completely without mandatory internet or cloud dependencies.

```mermaid
graph TD
    subgraph Users
        Driver["Fleet Vehicle Driver<br/>[Person]<br/>Mounts phone in forward windshield dock and initiates scans."]
        Supervisor["Fleet Supervisor / Reviewer<br/>[Person]<br/>Reviews road defects, verifies alerts, and inspects reports."]
    end

    subgraph RoadLens Ecosystem
        RoadLensApp["RoadLens Mobile Application<br/>[Software System]<br/>Provides on-device AI computer vision, distress clustering, and road hazard analytics."]
    end

    subgraph Environment & Hardware
        CameraSensor["Phone Camera / Hardware Decoder<br/>[Hardware Component]<br/>Streams forward-facing road video frames."]
        GPSProvider["GNSS / Location Provider<br/>[Hardware Component]<br/>Emits latitude, longitude, and accuracy fixes."]
        FileStorage["App-Private Storage<br/>[Local File System]<br/>Stores annotated JPG evidence and exported CSV/PDF reports."]
    end

    Driver -->|"Operates scan & mounts phone"| RoadLensApp
    Supervisor -->|"Audits road distresses & exports reports"| RoadLensApp
    CameraSensor -->|"Supplies video / camera frames"| RoadLensApp
    GPSProvider -->|"Provides location metadata"| RoadLensApp
    RoadLensApp -->|"Writes evidence and reports"| FileStorage
```

---

## 2. C4 Level 2: Container Diagram

The Container diagram illustrates the high-level technical architecture of the RoadLens Android application container.

```mermaid
graph TD
    subgraph RoadLens Application Container [Native Android / Kotlin]
        UI["Presentation Layer (Jetpack Compose)<br/>[Component: Jetpack Compose / M3]<br/>Renders Dashboard, Scan Canvas, Timeline, and Hazard Overlays."]
        
        ViewModel["State & Orchestration (ScanViewModel)<br/>[Component: Architecture Component ViewModel]<br/>Coordinates UI state, video scrubbing, and temporal deduplication heuristics."]
        
        Perception["Perception Engine (RoadDefectDetector)<br/>[Component: Kotlin Interface & OnnxYoloDetector]<br/>Encapsulates AI execution, zero-allocation preprocessor, and NMS postprocessor."]
        
        ONNXRuntime["Microsoft ONNX Runtime Android<br/>[Library: com.microsoft.onnxruntime 1.23.2]<br/>Executes quantized/FP16 YOLO11n neural network graph on CPU/NNAPI."]
        
        ModelAsset["Model Asset (yolo11n.onnx)<br/>[Binary Asset]<br/>Trained YOLO11n weights detecting D00, D10, D20, D40 distress classes."]
        
        MediaCanvas["Video Canvas (LiveVideoDetectorCanvas)<br/>[Component: TextureView + MediaPlayer]<br/>Hardware-accelerated media decoding and live bounding box canvas."]
        
        DomainUtils["Domain Geometry & Time Utilities<br/>[Component: GeometryUtils & TimeFormatter]<br/>Calculates IoU, center distances, and formatted timeline timestamps."]
    end

    UI -->|"Observes StateFlow & dispatches actions"| ViewModel
    UI -->|"Hosts Surface & Canvas"| MediaCanvas
    MediaCanvas -->|"Delegates frame detection"| Perception
    MediaCanvas -->|"Reports detected objects & timestamps"| ViewModel
    ViewModel -->|"Applies deduplication logic"| DomainUtils
    Perception -->|"Passes NCHW tensor buffer"| ONNXRuntime
    ONNXRuntime -->|"Loads model weights"| ModelAsset
    Perception -->|"Computes IoU for NMS"| DomainUtils
```
