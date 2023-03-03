@file:Suppress("UnstableApiUsage")

plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
    id("de.mannodermaus.android-junit5")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.settings"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:art"))
    api(project(":modules:cds"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    implementation("com.google.dagger:hilt-android:2.45")
    kapt("com.google.dagger:hilt-compiler:2.45")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("androidx.compose.ui:ui:1.4.0-beta01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0-beta01")
    implementation("androidx.compose.material3:material3:1.1.0-alpha06")

    api("androidx.work:work-runtime:2.8.0")
    api("androidx.work:work-runtime-ktx:2.8.0")

    testImplementation(project(":modules:test-utils"))
    testCompileOnly(testFixtures(project(":modules:art")))
    testImplementation(project(":modules:art", "_testFixtures"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.3.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.3.0")
}