plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.anime.ignore.data)
            api(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.ignore.testing"
}
