plugins {
    id("module-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.android_utils"
}

dependencies {
    api(project(":modules:utils"))
    api(project(":modules:utils-room"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    runtimeOnly(libs.work.runtime)
    api(libs.work.runtime.ktx)
    api(libs.flowExt)
    api(libs.androidx.security.crypto)

    api(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    runtimeOnly(libs.kotlinx.coroutines.android)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    compileOnly(libs.compose.runtime)
}
