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
            api(libs.paging.common)
            implementation(project(":modules:utils"))
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
