plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.activity.compose)
        }
        commonMain.dependencies {
            api(compose.components.resources)
            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.kermit)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils_compose"
}

compose.resources {
    publicResClass = true
}
