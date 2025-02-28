plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.browse"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.modules.entry)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
        }
    }
}
