plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
}

kotlin {
    // TODO: This target is required to resolve kotlin-test properly, but why?
    iosX64()

    sourceSets {
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
