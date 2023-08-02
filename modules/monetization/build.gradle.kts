plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.monetization"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:compose-utils"))

    implementation(libs.hilt.android)
    kapt(kaptProcessors.dagger.hilt.compiler)
    kapt(kaptProcessors.androidx.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material.icons.core)
    implementation(libs.material.icons.extended)
    implementation(libs.material3)
}
