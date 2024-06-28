@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.library")
    kotlin("android")
    id("android-test-library")
}

android {
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner =
            "com.thekeeperofpie.artistalleydatabase.test_utils.CustomAndroidJUnitRunner"
        project.file("consumer-rules.pro")
            .takeIf(File::exists)
            ?.let { consumerProguardFiles(it) }

        testInstrumentationRunnerArguments["runnerBuilder"] =
            "com.thekeeperofpie.artistalleydatabase.test_utils.AndroidJUnitBuilder"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                *listOfNotNull(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    project.file("proguard-rules.pro")
                        .takeIf(File::exists)
                ).toTypedArray()
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xcontext-receivers"
    }
    packaging {
        resources {
            merges += "/META-INF/*"
            merges += "mozilla/public-suffix-list.txt"

            // Can happen if an archive was built incrementally and accidentally published as-is
            excludes += "**/previous-compilation-data.bin"

            // Kotlin coroutines test
            pickFirsts += "win32-x86-64/attach_hotspot_windows.dll"
            pickFirsts += "win32-x86/attach_hotspot_windows.dll"

            // Unknown
            pickFirsts += "META-INF/licenses/ASM"

            // Mockito inline
            pickFirsts += "mockito-extensions/org.mockito.plugins.MockMaker"
        }
    }
}

kotlin {
    jvmToolchain(18)
    sourceSets.all {
        languageSettings {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

dependencies {
    // TODO: Figure this out and remove
    // https://github.com/gradle/gradle/issues/22326
    // AGP 8.3.0-alpha11 causes a conflicts with Guava
    modules {
        module("com.google.guava:listenablefuture") {
            replacedBy("com.google.guava:guava")
        }
    }
}

// The KSP jvmTarget isn't set correctly, so fix it up here
afterEvaluate {
    tasks.withType(KotlinCompile::class).forEach {
        it.kotlinOptions {
            jvmTarget = "11"
        }
    }
}
