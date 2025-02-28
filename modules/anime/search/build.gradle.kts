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
            api(projects.modules.anime.characters.data)
            api(projects.modules.anime.media.data)
            api(projects.modules.anime.search.data)
            api(projects.modules.anime.staff.data)
            api(projects.modules.anime.studios.data)
            api(projects.modules.anime.users.data)
            implementation(projects.modules.utils)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.search"
}

compose.resources {
    publicResClass = true
}
