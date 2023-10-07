plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
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

    api(libs.material)
    api(libs.material3)
    api(libs.compose.ui)
    api(libs.compose.ui.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.material.icons.extended)

    api(libs.coil.compose)
    implementation(libs.palette.ktx)
    implementation(libs.html.text)
}
