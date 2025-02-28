plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-room")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.anilist)
            api(projects.modules.browse)
            api(projects.modules.data)
            api(projects.modules.entry)
            api(projects.modules.musicalArtists)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsRoom)
            api(projects.modules.vgmdb)

            implementation(libs.jetBrainsAndroidX.navigation.compose)
            implementation(libs.coil3.coil.compose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.cds"
}
