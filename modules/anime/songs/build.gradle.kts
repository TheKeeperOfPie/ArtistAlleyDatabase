plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.anilist)
            implementation(projects.modules.anime.ui)
            implementation(projects.modules.anime.characters.data)
            implementation(projects.modules.media)
            implementation(projects.modules.utils)
            implementation(libs.coil3.coil.compose)
            implementation(libs.jetBrainsCompose.navigation.compose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.songs"
}

compose.resources {
    publicResClass = true
}
