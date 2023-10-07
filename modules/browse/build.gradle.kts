plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.browse"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:compose-utils"))
    implementation(project(":modules:entry"))

    api(libs.navigation.compose)

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.paging.compose)
}
