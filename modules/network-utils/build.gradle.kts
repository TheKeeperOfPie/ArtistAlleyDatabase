plugins {
    id("module-library")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.network_utils"
}

dependencies {
    api(libs.okhttp)
    implementation(libs.okhttp3.logging.interceptor)

    // Cronet embedded causes a weird infinite loop when syncing in IDE, so use a local AAR
    runtimeOnly(group = "", name = "cronet-embedded-108.5359.79", ext = "aar")
    runtimeOnly(group = "", name = "cronet-common-108.5359.79", ext = "aar")
    implementation(libs.cronet.okhttp)

    api(libs.hilt.android)
    kapt(kaptProcessors.dagger.hilt.compiler)
    kapt(kaptProcessors.androidx.hilt.compiler)
    kapt(kaptProcessors.dagger.hilt.compiler)
}
