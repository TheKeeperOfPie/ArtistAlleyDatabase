plugins {
    id("module-library")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.animethemes"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:anime"))
    api(project(":modules:network-utils"))
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    api(libs.okhttp)
}
