plugins {
    id("module-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.musical_artists"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    ksp(kspProcessors.dagger.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    androidTestRuntimeOnly(libs.androidx.test.runner)
}
