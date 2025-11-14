plugins {
    id("library-android")
    id("library-desktop")
    id("library-web")
}

kotlin {
    androidLibrary { namespace = "com.thekeeperofpie.artistalleydatabase.alley.models" }

    sourceSets {
        commonMain.dependencies {
            api("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
