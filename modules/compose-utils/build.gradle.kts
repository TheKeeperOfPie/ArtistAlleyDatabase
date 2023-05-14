plugins {
    id("compose-library")
}

android {
    namespace = "com.thekeeperofpie.compose_proxy"
}

dependencies {
    implementation(project(":modules:android-utils"))
    api(libs.material)
    api(libs.material3)
    api(libs.compose.ui)
    api(libs.compose.ui.tooling.preview)
    implementation(libs.activity.compose)

    api(libs.paging.compose)
    api(libs.coil.compose)
    implementation(libs.palette.ktx)
}
