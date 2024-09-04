plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.activity.compose)
            implementation(libs.palette.ktx)
        }
        commonMain.dependencies {
            api(compose.components.resources)
            api(libs.paging.common)
            implementation(project(":modules:utils"))
            implementation(libs.coil3.coil.compose)
            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.kermit)
            implementation(libs.colormath.ext.jetpack.compose)
        }
        desktopMain.dependencies {
            implementation(libs.kmpalette.core)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils_compose"
}

compose.resources {
    publicResClass = true
}
