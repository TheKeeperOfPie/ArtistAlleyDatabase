plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.de.mannodermaus.android.junit5)
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.entry"
}

dependencies {
    api(project(":modules:android-utils"))
    implementation(project(":modules:compose-utils"))

    api(libs.flowExt)
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)
    api(libs.compose.animation)
    api(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.material3)

    implementation(libs.navigation.compose)
    implementation(libs.paging.compose)
    implementation(libs.coil.compose)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.java.diff.utils)

    androidTestImplementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

//    implementation(libs.shared-elements)
    implementation(group = "", name = "shared-elements-0.1.0-20221204.093513-11", ext = "aar")
}
