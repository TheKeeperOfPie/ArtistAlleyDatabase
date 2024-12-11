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
            api(projects.modules.anime.staff.data)
            api(projects.modules.anime.studios.data)
            implementation(projects.modules.anime.ui)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.users"
}

compose.resources {
    publicResClass = true
}
