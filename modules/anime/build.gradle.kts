plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anilist"))
    api(project(":modules:animethemes"))
    implementation(project(":modules:compose-utils"))
    api(project(":modules:cds"))

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    kapt(kaptProcessors.dagger.hilt.compiler)
    kapt(kaptProcessors.androidx.hilt.compiler)

    implementation(libs.lifecycle.viewmodel.ktx)

    api(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    runtimeOnly(libs.compose.ui.tooling)
    implementation(libs.accompanist.flowlayout)

    implementation(libs.coil.compose)
    implementation(libs.material.icons.extended)
    implementation(libs.accompanist.placeholder.material)
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.palette.ktx)

    implementation(libs.hilt.navigation.compose)

    implementation(group = "", name = "shared-elements-0.1.0-20221204.093513-11", ext = "aar")

    implementation(libs.html.text)

    api(libs.media3.exoplayer)
    runtimeOnly(libs.media3.datasource.okhttp)
    runtimeOnly(libs.media3.exoplayer.dash)
    runtimeOnly(libs.media3.exoplayer.hls)
    runtimeOnly(libs.media3.exoplayer.rtsp)
    implementation(libs.media3.ui)
}
