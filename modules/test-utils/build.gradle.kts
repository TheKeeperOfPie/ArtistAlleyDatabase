plugins {
    id("module-library")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.test_utils"
}

dependencies {
    implementation(project(":modules:android-utils"))
    api(libs.mockito.core)
    api(libs.mockito.android)
    implementation(libs.mockito.kotlin)
    api(libs.awaitility) {
        exclude("org.hamcrest", "hamcrest")
    }
    api(kotlin("reflect"))
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.test)
}