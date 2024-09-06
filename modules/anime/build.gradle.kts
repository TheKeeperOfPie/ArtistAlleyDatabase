plugins {
    id("compose-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.de.mannodermaus.android.junit5)
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime"
}

dependencies {
    api(project(":modules:anilist"))
    api(project(":modules:cds"))
    api(project(":modules:markdown"))
    api(project(":modules:media"))
    api(project(":modules:monetization"))
    api(project(":modules:news"))
    api(project(":modules:utils-compose"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.json.io)

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    api(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    runtimeOnly(libs.compose.ui.tooling)

    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)
    implementation(libs.material3)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.hilt.navigation.compose)

    implementation(libs.fluid.country)
    implementation(libs.fluid.i18n)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)

    androidTestImplementation(libs.junit.jupiter.params)
    androidTestImplementation(libs.junit5.android.test.compose)
    debugRuntimeOnly(libs.compose.ui.test.manifest)
    kspAndroidTest(kspProcessors.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android.testing)

    // Resolves a missing method exception during testing
    debugImplementation(libs.androidx.tracing)

    implementation(libs.kermit)
    implementation(libs.human.readable)
    implementation(libs.compose.placeholder.material3)
    implementation(libs.stately.concurrent.collections)
}
