plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    id(libs.plugins.org.jetbrains.kotlin.plugin.parcelize.get().pluginId)
}

android {
    namespace = "com.thekeeperofpie.compose_proxy"
}

dependencies {
    api(project(":modules:android-utils"))
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    api(libs.material3)
    api(libs.compose.ui)
    api(libs.compose.ui.tooling.preview)
    api(libs.compose.animation)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.compose.material.icons.extended)

    api(libs.coil3.coil.compose)
    api(libs.coil3.coil.network.okhttp)
    implementation(libs.palette.ktx)
    implementation(libs.html.text)

    api(libs.paging.compose)

    implementation(libs.molecule.runtime)

    api(group = "", name = "shared-elements-0.1.0-20221204.093513-11", ext = "aar")
}
