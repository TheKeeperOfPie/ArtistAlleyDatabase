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
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.studios.data"
}
