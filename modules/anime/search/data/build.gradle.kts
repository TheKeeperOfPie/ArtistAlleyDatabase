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
            api(projects.modules.anime.media.data)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.search.data"
}

compose.resources {
    publicResClass = true
}
