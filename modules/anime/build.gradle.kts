plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.de.mannodermaus.android.junit5)
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anilist"))
    api(project(":modules:compose-utils"))
    api(project(":modules:cds"))
    api(project(":modules:markdown"))
    api(project(":modules:monetization"))
    api(project(":modules:news"))

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    api(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    runtimeOnly(libs.compose.ui.tooling)
    implementation(libs.accompanist.flowlayout)

    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.hilt.navigation.compose)
    api(libs.paging.compose)

    implementation(libs.nv.i18n)

    api(libs.media3.exoplayer)
    implementation(libs.media3.datasource.okhttp)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.exoplayer.rtsp)
    implementation(libs.media3.ui)
    implementation(libs.androidyoutubeplayer)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)

    androidTestImplementation(libs.junit.jupiter.params)
    androidTestImplementation(libs.junit5.android.test.compose)
    debugRuntimeOnly(libs.compose.ui.test.manifest)
    kspAndroidTest(kspProcessors.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android.testing)

    // Resolves a missing method exception during testing
    debugImplementation(libs.androidx.tracing)
}
