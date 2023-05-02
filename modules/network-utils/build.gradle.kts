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

    runtimeOnly(libs.cronet.embedded)
    implementation(libs.cronet.okhttp)

    api(libs.hilt.android)
    kapt(kaptProcessors.dagger.hilt.compiler)
    kapt(kaptProcessors.androidx.hilt.compiler)
    kapt(kaptProcessors.dagger.hilt.compiler)
}
