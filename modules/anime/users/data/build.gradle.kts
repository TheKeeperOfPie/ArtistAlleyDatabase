plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.anilist)
            api(projects.modules.utilsCompose)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.anime.users.data"
    }
}

compose.resources {
    publicResClass = true
}
