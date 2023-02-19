@file:Suppress("UnstableApiUsage")

plugins {
    id("compose-library")
}

android {
    namespace = "com.thekeeperofpie.compose_proxy"
}

dependencies {
    api("androidx.compose.material:material:1.4.0-beta01")
    api("androidx.compose.material3:material3:1.1.0-alpha06")
    api("androidx.compose.ui:ui:1.4.0-beta01")
    api("androidx.compose.ui:ui-tooling-preview:1.4.0-beta01")
    implementation("androidx.activity:activity-compose:1.8.0-alpha01")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.29.1-alpha")

    api("androidx.paging:paging-compose:1.0.0-alpha18")
}