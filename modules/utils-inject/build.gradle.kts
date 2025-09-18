plugins {
    id("library-android")
    id("library-desktop")
    id("library-web")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.inject.runtime.kmp)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.inject"
    }
}
