plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.musical_artists"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            runtimeOnly(libs.room.runtime)
            implementation(libs.room.ktx)
            implementation(libs.room.paging)
        }
    }
}

compose.resources {
    publicResClass = true
}
