plugins {
    id("module-library")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.network_utils"

    buildTypes {
        create("internal")
    }
}

dependencies {
    api(libs.okhttp)
    implementation(libs.okhttp3.logging.interceptor)

    releaseRuntimeOnly(libs.cronet.play)
    "internalRuntimeOnly"(libs.cronet.embedded)
    debugRuntimeOnly(libs.cronet.embedded)
    implementation(libs.cronet.okhttp)

    api(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)
    ksp(kspProcessors.hilt.compiler)
}
