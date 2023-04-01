plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime"
}

dependencies {
    implementation(project(":modules:android-utils"))
    api(project(":modules:anilist"))
    implementation(project(":modules:compose-utils"))

    implementation(libs.hilt.android)
    kapt(kaptProcessors.dagger.hilt.compiler)
    kapt(kaptProcessors.androidx.hilt.compiler)

    implementation(libs.lifecycle.viewmodel.ktx)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    runtimeOnly(libs.compose.ui.tooling)
    implementation(libs.material3)

    implementation(libs.coil.compose)
    implementation(libs.material.icons.extended)

    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
}
