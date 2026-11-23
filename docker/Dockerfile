# Dockerfile for RoadLens Automated Headless Build & Verification Environment (KPI 2.6)
FROM eclipse-temurin:17-jdk-jammy

# Set non-interactive debian frontend
ENV DEBIAN_FRONTEND=noninteractive

# Set environment variables for Android SDK
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH="${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools"

# Install core build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    unzip \
    git \
    bash \
    file \
    && rm -rf /var/lib/apt/lists/*

# Download and install Android Command-Line Tools
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    curl -o /tmp/cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# Accept Android SDK licenses
RUN yes | sdkmanager --licenses > /dev/null

# Install required Android platforms and build tools
RUN sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Set working directory for app build
WORKDIR /app

# Copy repository source code
COPY . /app/

# Ensure gradlew has execute permission
RUN chmod +x ./gradlew

# Default execution: run automated unit tests, JaCoCo coverage, and assemble debug APK
CMD ["./gradlew", "testDebugUnitTest", "createDebugUnitTestCoverageReport", "assembleDebug", "--no-daemon"]
