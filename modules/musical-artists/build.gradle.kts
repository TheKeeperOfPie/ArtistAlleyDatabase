@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "1.7.20-Beta-1.0.6"
    kotlin("plugin.serialization") version "1.7.20-Beta"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.musical_artists"
    compileSdk = 33

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("com.google.dagger:hilt-android:2.43.2")
    kapt("com.google.dagger:hilt-compiler:2.43.2")

    runtimeOnly("androidx.room:room-runtime:2.5.0-beta02")
    ksp("androidx.room:room-compiler:2.5.0-beta02")
    implementation("androidx.room:room-ktx:2.5.0-beta02")
    testImplementation("androidx.room:room-testing:2.5.0-beta02")
    implementation("androidx.room:room-paging:2.5.0-beta02")

    androidTestImplementation("androidx.test:runner:1.5.1")
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.3.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.3.0")
}