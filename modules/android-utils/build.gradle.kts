plugins {
    id("module-library")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.android_utils"
}

dependencies {
    implementation(libs.core.ktx)

    runtimeOnly(libs.work.runtime)
    api(libs.work.runtime.ktx)
    api(libs.flowExt)
    api(libs.androidx.security.crypto)

    implementation(libs.kotlinx.serialization.json)
    runtimeOnly(libs.kotlinx.coroutines.android)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    api(libs.moshi.kotlin)
    ksp(kspProcessors.moshi.kotlin.codegen)

    api(libs.okhttp)
    implementation(libs.okhttp3.logging.interceptor)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.test)
}