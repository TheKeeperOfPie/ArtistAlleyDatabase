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
            implementation(compose.components.resources)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils_compose"
}
