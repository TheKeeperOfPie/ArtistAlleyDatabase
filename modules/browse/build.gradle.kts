@file:Suppress("UnstableApiUsage")

plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.browse"
}

dependencies {
    api(project(":modules:android-utils"))
    implementation(project(":modules:compose-utils"))
    implementation(project(":modules:entry"))

    api("androidx.navigation:navigation-compose:2.6.0-alpha06")

    implementation("com.google.dagger:hilt-android:2.45")
    kapt("com.google.dagger:hilt-compiler:2.45")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("androidx.compose.ui:ui:1.4.0-beta02")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0-beta02")
    implementation("androidx.compose.material3:material3:1.1.0-alpha07")

    implementation("com.google.accompanist:accompanist-pager:0.29.1-alpha")
    // TODO: Re-add official pager-indicator library once it migrates to material3
    // implementation("com.google.accompanist:accompanist-pager-indicators:0.24.13-rc")
}