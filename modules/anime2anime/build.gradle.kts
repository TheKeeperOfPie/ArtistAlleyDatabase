plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.de.mannodermaus.android.junit5)
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime2anime"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anilist"))
    api(project(":modules:anime"))
    api(project(":modules:compose-utils"))

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    implementation(libs.lifecycle.viewmodel.ktx)

    api(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    runtimeOnly(libs.compose.ui.tooling)
    implementation(libs.accompanist.flowlayout)

    implementation(libs.coil.compose)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.hilt.navigation.compose)
    api(libs.paging.compose)

    debugRuntimeOnly(libs.compose.ui.test.manifest)
    kspAndroidTest(kspProcessors.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android.testing)
}
