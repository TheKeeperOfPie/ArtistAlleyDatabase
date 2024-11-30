plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.activity.compose)
            implementation(libs.html.text)
            implementation(libs.palette.ktx)
        }
        commonMain.dependencies {
            api(compose.components.resources)
            implementation(projects.modules.secrets)
            implementation(projects.modules.utils)
            implementation(libs.coil3.coil.compose)
            implementation(libs.colormath.ext.jetpack.compose)
            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.molecule.runtime)
            implementation(libs.paging.common)
        }
        desktopMain.dependencies {
            implementation(libs.human.readable)
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
