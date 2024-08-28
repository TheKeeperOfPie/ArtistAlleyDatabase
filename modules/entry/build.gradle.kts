plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.de.mannodermaus.android.junit5)
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.entry"
}

dependencies {
    implementation(project(":modules:utils"))
    implementation(project(":modules:utils-compose"))
    implementation(project(":modules:utils-room"))

    api(libs.flowExt)
    implementation(libs.activity.compose)
    implementation(libs.androidx.core.ktx)
    api(libs.compose.animation)
    api(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.material3)

    implementation(libs.navigation.compose)
    implementation(libs.paging.compose)
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.uuid)
    implementation(libs.uri.kmp)

    implementation(libs.java.diff.utils)

    androidTestImplementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)
}
