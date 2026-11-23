# SOLID Principles & Software Design Patterns in RoadLens (KPI 2.3 & 2.4)

This document explains the application of the 5 SOLID object-oriented design principles and software design patterns within the RoadLens codebase.

---

## Part 1: The 5 SOLID Principles Explained & Code Demonstrations

### 1. Single Responsibility Principle (SRP)
* **Definition:** A class or module should have one, and only one, reason to change.
* **Demonstrated Application in RoadLens:**
  Instead of packing preprocessing, model execution, and tensor decoding into a monolithic class, the perception pipeline is strictly decoupled:
  - [`YoloPreprocessor.kt`](file:///C:/Users/rushna.fathima/AndroidStudioProjects/RoadLens/app/src/main/java/com/roadlens/app/ai/YoloPreprocessor.kt): Sole responsibility is letterbox resizing, zero-allocation memory buffering, and planar NCHW float normalization.
  - [`YoloPostprocessor.kt`](file:///C:/Users/rushna.fathima/AndroidStudioProjects/RoadLens/app/src/main/java/com/roadlens/app/ai/YoloPostprocessor.kt): Sole responsibility is un-letterboxing coordinate transforms, candidate thresholding ($\ge 0.25$), and Non-Maximum Suppression (NMS).
  - [`OnnxYoloDetector.kt`](file:///C:/Users/rushna.fathima/AndroidStudioProjects/RoadLens/app/src/main/java/com/roadlens/app/ai/OnnxYoloDetector.kt): Sole responsibility is managing the ONNX Runtime session lifecycle and orchestrating pre- and post-processing.

---

### 2. Open/Closed Principle (OCP)
* **Definition:** Software entities should be open for extension, but closed for modification.
* **Demonstrated Application in RoadLens:**
  The [`RoadDefectDetector.kt`](file:///C:/Users/rushna.fathima/AndroidStudioProjects/RoadLens/app/src/main/java/com/roadlens/app/ai/RoadDefectDetector.kt) interface establishes an immutable perception contract:
  ```kotlin
  interface RoadDefectDetector : AutoCloseable {
      fun detectOnly(bitmap: Bitmap): List<RoadDetection>
      fun detect(bitmap: Bitmap): DetectionResult
  }
  ```
  New inference backends (e.g. `TfLiteRoadDetector`, `QualcommNpuDetector`, or `FakeDetector` for automated unit tests) can be added without modifying a single line of `ScanViewModel` or UI canvas code.

---

### 3. Liskov Substitution Principle (LSP)
* **Definition:** Objects of a superclass should be replaceable with objects of a subclass without altering the correctness of the program.
* **Demonstrated Application in RoadLens:**
  In [`ScanViewModelTest.kt`](file:///C:/Users/rushna.fathima/AndroidStudioProjects/RoadLens/app/src/test/java/com/roadlens/app/ui/viewmodel/ScanViewModelTest.kt), `FakeDetector` implements `RoadDefectDetector`. `ScanViewModel` interacts with `FakeDetector` identically to `OnnxYoloDetector`, preserving all state transitions, deduplication rules, and timeline operations without runtime errors or behavioral regressions.

---

### 4. Interface Segregation Principle (ISP)
* **Definition:** Clients should not be forced to depend upon interfaces that they do not use.
* **Demonstrated Application in RoadLens:**
  `RoadDefectDetector` extends `AutoCloseable`, exposing only the two essential query methods (`detectOnly` and `detect`) and `close()`. Consumers like `LiveVideoDetectorCanvas` only need lightweight bounding box coordinate lists and call `detectOnly(bitmap)` to skip unnecessary bitmap annotation allocations.

---

### 5. Dependency Inversion Principle (DIP)
* **Definition:** High-level modules should not depend upon low-level modules; both should depend upon abstractions. Abstractions should not depend upon details; details should depend upon abstractions.
* **Demonstrated Application in RoadLens:**
  [`ScanViewModel.kt`](file:///C:/Users/rushna.fathima/AndroidStudioProjects/RoadLens/app/src/main/java/com/roadlens/app/ui/viewmodel/ScanViewModel.kt) (high-level orchestration) does not depend on the concrete `OnnxYoloDetector` (low-level ONNX Runtime C++ wrapper). Instead, it depends strictly on the abstraction `RoadDefectDetector`:
  ```kotlin
  class ScanViewModel(
      private val detector: RoadDefectDetector
  ) : ViewModel()
  ```
  Dependency injection is achieved via the `ViewModelProvider.Factory` pattern in [`MainActivity.kt`](file:///C:/Users/rushna.fathima/AndroidStudioProjects/RoadLens/app/src/main/java/com/roadlens/app/MainActivity.kt).

---

## Part 2: Software Design Patterns Implemented

### 1. Strategy Design Pattern (Behavioral)
* **Intent:** Define a family of algorithms, encapsulate each one, and make them interchangeable.
* **Implementation in RoadLens:**
  - **Strategy Interface:** [`RoadDefectDetector`](file:///C:/Users/rushna.fathima/AndroidStudioProjects/RoadLens/app/src/main/java/com/roadlens/app/ai/RoadDefectDetector.kt)
  - **Concrete Strategies:**
    1. `OnnxYoloDetector`: Employs quantized YOLO11n executed on CPU via Microsoft ONNX Runtime.
    2. `FakeDetector`: Test double used during deterministic unit testing.
  - **Context:** `ScanViewModel` and `LiveVideoDetectorCanvas`.

### 2. Observer Design Pattern (Behavioral)
* **Intent:** Maintain a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically.
* **Implementation in RoadLens:**
  - `ScanViewModel` exposes state via Kotlin Coroutines `StateFlow<ScanScreenUiState>`.
  - Jetpack Compose screens (`ScanScreen`, `HomeScreen`) observe the flow via `collectAsState()`, automatically triggering targeted recomposition whenever new frames are analyzed or timeline items are added.
