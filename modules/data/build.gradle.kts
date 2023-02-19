@file:Suppress("UnstableApiUsage")

plugins {
    id("module-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.data"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anilist"))
    api(project(":modules:entry"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    implementation("com.google.dagger:hilt-android:2.45")
    kapt("com.google.dagger:hilt-compiler:2.45")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}