plugins {
    id("module-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.vgmdb"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:entry"))
    api(project(":modules:utils-network"))
    api(project(":modules:utils-compose"))

    api(libs.okhttp)

    implementation(libs.compose.ui)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    implementation(libs.skrapeit)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.truth)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)

    androidTestRuntimeOnly(libs.junit5.android.test.runner)
}
