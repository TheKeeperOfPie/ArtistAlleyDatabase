@file:Suppress("UnstableApiUsage")

plugins {
    id("module-library")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.android_utils"
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")

    runtimeOnly("androidx.work:work-runtime:2.8.0")
    api("androidx.work:work-runtime-ktx:2.8.0")
    api("io.github.hoc081098:FlowExt:0.5.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    runtimeOnly("androidx.room:room-runtime:2.5.0")
    ksp("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    testImplementation("androidx.room:room-testing:2.5.0")
    implementation("androidx.room:room-paging:2.5.0")

    api("com.squareup.moshi:moshi-kotlin:1.14.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}