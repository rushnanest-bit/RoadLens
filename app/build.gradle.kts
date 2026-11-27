plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("jacoco")
}

android {
    namespace = "com.roadlens.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.roadlens.app"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ONNX Runtime for AI inference
    implementation(libs.onnxruntime.android)

    // Room SQLite Persistence
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // CameraX Live Scan
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // GPS Location Services
    implementation(libs.play.services.location)

    // MapLibre Native Android
    implementation(libs.maplibre.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jacocoTestReport/html"))
    }

    val coverageExclusions = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        // Room generated code (DAOs, DB implementations)
        "**/*_Impl*.*",
        "**/*Dao_Impl*.*",
        "**/*Database_Impl*.*",
        // Pure declarative Jetpack Compose UI layout components and screens
        "**/ui/components/**",
        "**/ui/screens/**",
        "**/ui/theme/**",
        "**/ui/navigation/**",
        "**/*ComposableSingletons*.*",
        "**/MainActivity*.*",
        // Hardware sensors and platform location wrappers
        "**/data/location/**"
    )

    val kotlinDebugClasses = fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
        exclude(coverageExclusions)
    }
    val javaDebugClasses = fileTree(layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")) {
        exclude(coverageExclusions)
    }

    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    classDirectories.setFrom(files(kotlinDebugClasses, javaDebugClasses))
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include(
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
            "jacoco/testDebugUnitTest.exec"
        )
    })

    doLast {
        val xmlReport = reports.xml.outputLocation.get().asFile
        if (xmlReport.exists()) {
            val dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            val doc = dbf.newDocumentBuilder().parse(xmlReport)
            val counters = doc.getElementsByTagName("counter")
            println("\n" + "=".repeat(45))
            println("       ROADLENS UNIT TEST COVERAGE")
            println("=".repeat(45))
            for (i in 0 until counters.length) {
                val node = counters.item(i)
                if (node.parentNode.nodeName == "report") {
                    val type = node.attributes.getNamedItem("type").nodeValue
                    val missed = node.attributes.getNamedItem("missed").nodeValue.toInt()
                    val covered = node.attributes.getNamedItem("covered").nodeValue.toInt()
                    val total = missed + covered
                    val pct = if (total > 0) (covered.toDouble() / total) * 100 else 0.0
                    println(String.format("%-15s : %6.2f%% (%d / %d)", type, pct, covered, total))
                }
            }
            println("=".repeat(45) + "\n")
        }
    }
}

