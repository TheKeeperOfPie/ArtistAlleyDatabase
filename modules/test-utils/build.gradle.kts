plugins {
    id("module-library")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.test_utils"
}

dependencies {
    api(project(":modules:android-utils"))
    implementation(project(":modules:network-utils"))
    implementation(project(":modules:server"))

    implementation(libs.dexmaker.mockito.inline.extended)
    implementation(libs.mockito.kotlin)

    api(libs.awaitility) {
        exclude("org.hamcrest", "hamcrest")
    }
    api(kotlin("reflect"))
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.test)
    api(libs.junit.jupiter.api)
    api(libs.androidx.test.runner)
    implementation(libs.junit5.android.test.runner)

    api(libs.truth)
    api(libs.hilt.android.testing)
    ksp(kspProcessors.hilt.android.compiler)

    api(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)

    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.server.test.host)
}
