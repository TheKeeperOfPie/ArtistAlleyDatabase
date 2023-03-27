plugins {
    id("compose-library")
}

android {
    namespace = "com.thekeeperofpie.compose_proxy"
}

dependencies {
    api(libs.material)
    api(libs.material3)
    api(libs.compose.ui)
    api(libs.compose.ui.tooling.preview)
    implementation(libs.activity.compose)

    api(libs.paging.compose)
}