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
    api(project(":modules:compose-utils"))
    api(project(":modules:cds"))
    api(project(":modules:monetization"))
    implementation(project(":modules:network-utils"))

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

    implementation(libs.hilt.navigation.compose)
    api(libs.paging.compose)

    implementation(libs.nv.i18n)
    implementation(libs.constraintlayout.compose)

    api(libs.media3.exoplayer)
    implementation(libs.media3.datasource.cronet)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.exoplayer.rtsp)
    implementation(libs.media3.ui)
    implementation(libs.androidyoutubeplayer)

    implementation(group = "", name = "shared-elements-0.1.0-20221204.093513-11", ext = "aar")

    implementation(libs.rome)

    api(libs.markwon.core)
//    implementation(libs.markwon.editor)
    implementation(libs.markwon.ext.strikethrough)
    implementation(libs.markwon.ext.tables)
    api(libs.markwon.html)
    implementation(libs.markwon.image.coil)
    implementation(libs.markwon.linkify)
//    implementation(libs.markwon.simple.ext)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
}
