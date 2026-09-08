plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-web")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.coil3.coil.compose)
        }
    }
}
