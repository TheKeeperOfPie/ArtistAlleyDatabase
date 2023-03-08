@file:Suppress("UnstableApiUsage")

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
    implementation(libs.accompanist.pager.indicators)

    api(libs.paging.compose)
}