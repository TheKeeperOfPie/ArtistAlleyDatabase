@file:Suppress("UnstableApiUsage")

plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.cds"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anilist"))
    api(project(":modules:browse"))
    implementation(project(":modules:compose-utils"))
    api(project(":modules:data"))
    api(project(":modules:entry"))
    api(project(":modules:musical-artists"))
    api(project(":modules:vgmdb"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")

    implementation("com.google.dagger:hilt-android:2.45")
    kapt("com.google.dagger:hilt-compiler:2.45")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0-alpha01")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.compose.ui:ui:1.4.0-beta01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0-beta01")
    implementation("androidx.compose.material3:material3:1.1.0-alpha06")

    runtimeOnly("androidx.room:room-runtime:2.5.0")
    ksp("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    testImplementation("androidx.room:room-testing:2.5.0")
    implementation("androidx.room:room-paging:2.5.0")

    api("com.squareup.moshi:moshi-kotlin:1.14.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}