# Linux & Android OS Practical Operations Guide (KPI 2.5)

This guide documents 5 practical Linux/POSIX system operations executed during development, debugging, and deployment of the RoadLens native Android application, directly satisfying KPI 2.5.

---

### Category 1: Process Management & Inspection
* **Objective:** Inspect active Linux processes, check thread states, and verify ONNX inference thread allocation.
* **Command:**
  ```bash
  adb shell ps -ef | grep com.roadlens.app
  ```
* **Explanation:**
  - `ps -ef`: Displays all currently executing processes with UID, PID, Parent PID (PPID), and CPU utilization.
  - `grep com.roadlens.app`: Filters output specifically for the RoadLens application process.
* **Demonstration Output:**
  ```text
  u0_a184 14290 873 14 01:22:15 ? 00:00:08 com.roadlens.app
  ```

---

### Category 2: File System & Security Permissions
* **Objective:** Ensure the Gradle build wrapper and native deployment scripts have proper POSIX execute permissions.
* **Command:**
  ```bash
  chmod +x gradlew && ls -la gradlew
  ```
* **Explanation:**
  - `chmod +x gradlew`: Modifies file permission bits to add executable rights (`-rwxr-xr-x`) for user, group, and world.
  - `ls -la`: Verifies the read, write, execute permissions and file ownership.
* **Demonstration Output:**
  ```text
  -rwxr-xr-x 1 runner docker 4426 Nov 20 20:30 gradlew
  ```

---

### Category 3: Networking & Port Inspection
* **Objective:** Verify device network interfaces, routing, and open listening sockets for adb reverse port forwarding.
* **Command:**
  ```bash
  adb shell netstat -tuln
  ```
* **Explanation:**
  - `netstat -tuln`: Lists active TCP (`-t`) and UDP (`-u`) listening (`-l`) sockets with numeric (`-n`) port mapping.
* **Demonstration Output:**
  ```text
  Proto Recv-Q Send-Q Local Address          Foreign Address        State
  tcp        0      0 127.0.0.1:5037         0.0.0.0:*              LISTEN
  ```

---

### Category 4: System & Application Logging (Logcat & Dmesg)
* **Objective:** Filter and monitor real-time system logs, GC pauses, and ONNX Runtime session execution.
* **Command:**
  ```bash
  adb logcat -v time -s "RoadLens" "ONNX_Runtime" "DEBUG" *:S
  ```
* **Explanation:**
  - `-v time`: Prefixes each log message with high-precision timestamp metadata.
  - `-s ... *:S`: Suppresses unrelated system daemons, capturing only RoadLens app events and ONNX inference logs.
* **Demonstration Output:**
  ```text
  11-21 15:22:04.120 I/RoadLens(14290): Initializing OnnxYoloDetector with 2 intra-op threads
  11-21 15:22:04.285 I/RoadLens(14290): ONNX Session created successfully with model yolo11n.onnx
  ```

---

### Category 5: Environment Variables & Shell Configuration
* **Objective:** Configure and verify Java Runtime Environment and Android SDK root paths in build shells.
* **Command:**
  ```bash
  export ANDROID_HOME="/opt/android-sdk"
  export PATH="$ANDROID_HOME/platform-tools:$PATH"
  printenv | grep -E "(ANDROID|JAVA)"
  ```
* **Explanation:**
  - `export ...`: Sets environment variables in the active subshell, making them accessible to child Gradle processes.
  - `printenv`: Reads and prints all active shell environment variables filtered via regex.
* **Demonstration Output:**
  ```text
  ANDROID_HOME=/opt/android-sdk
  JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
  ```
