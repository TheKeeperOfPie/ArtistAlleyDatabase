plugins {
    id("compose-library")
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
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
    implementation(libs.material.icons.core)
    implementation(libs.material.icons.extended)
    implementation(libs.material3)

    implementation(libs.navigation.compose)
    implementation(libs.paging.compose)
    implementation(libs.coil.compose)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.java.diff.utils)

//    implementation(libs.shared-elements)
    implementation(group = "", name = "shared-elements-0.1.0-20221204.093513-11", ext = "aar")

    testImplementation(libs.junit)

    androidTestImplementation(project(":modules:test-utils"))
    androidTestImplementation(kotlin("test-junit"))
    androidTestImplementation(libs.androidx.junit.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestImplementation(libs.junit5.android.test.core)
    androidTestRuntimeOnly(libs.junit5.android.test.runner)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
