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
            api(projects.modules.anime.search.data)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.search"
}

compose.resources {
    publicResClass = true
}