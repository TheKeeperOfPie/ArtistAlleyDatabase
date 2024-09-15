plugins {
    id("library-android")
    id("library-desktop")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.apollo.runtime)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.apollo.utils"
}
