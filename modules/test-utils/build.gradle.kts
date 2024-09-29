plugins {
    id("library-android")
    id("library-kotlin")
    id("library-desktop")
    id("library-inject")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.test_utils"
}

dependencies {
    implementation(projects.modules.server)
    implementation(projects.modules.utilsNetwork)

    implementation(libs.dexmaker.mockito.inline.extended)
    implementation(libs.mockito.kotlin)

    api(libs.awaitility) {
        exclude("org.hamcrest", "hamcrest")
    }
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.test)
    api(libs.junit.jupiter.api)
    api(libs.androidx.test.runner)
    implementation(libs.junit5.android.test.runner)

    api(libs.truth)

    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.server.test.host)
}
