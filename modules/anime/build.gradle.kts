plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anilist"))
    implementation(project(":modules:compose-utils"))

    implementation(libs.hilt.android)
    kapt(kaptProcessors.dagger.hilt.compiler)
    kapt(kaptProcessors.androidx.hilt.compiler)

    implementation(libs.lifecycle.viewmodel.ktx)

    implementation(libs.compose.ui)
    api(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
}
