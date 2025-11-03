plugins {
    id("library-android")
    id("library-desktop")
    id("library-web")
    alias(libs.plugins.dev.zacsweers.metro)
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.inject"
    }
}
