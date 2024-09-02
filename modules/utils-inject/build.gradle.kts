plugins {
    id("library-android")
    id("library-desktop")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.inject.runtime.kmp)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.inject"
}
