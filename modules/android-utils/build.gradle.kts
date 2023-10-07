plugins {
    id("module-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.android_utils"
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    runtimeOnly(libs.work.runtime)
    api(libs.work.runtime.ktx)
    api(libs.flowExt)
    api(libs.androidx.security.crypto)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)
    runtimeOnly(libs.kotlinx.coroutines.android)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    api(libs.moshi.kotlin)
    ksp(kspProcessors.moshi.kotlin.codegen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.test)

    compileOnly(libs.compose.runtime)
}
