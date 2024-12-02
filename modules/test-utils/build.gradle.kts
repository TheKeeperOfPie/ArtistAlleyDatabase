plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.truth)
            implementation(projects.modules.utilsCompose)
            implementation(libs.turbine)
            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.test_utils"
}
