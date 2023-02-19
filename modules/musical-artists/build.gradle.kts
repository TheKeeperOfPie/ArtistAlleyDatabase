@file:Suppress("UnstableApiUsage")

plugins {
    id("module-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.musical_artists"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    implementation("com.google.dagger:hilt-android:2.45")
    kapt("com.google.dagger:hilt-compiler:2.45")

    runtimeOnly("androidx.room:room-runtime:2.5.0")
    ksp("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    testImplementation("androidx.room:room-testing:2.5.0")
    implementation("androidx.room:room-paging:2.5.0")

    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.3.0")
}