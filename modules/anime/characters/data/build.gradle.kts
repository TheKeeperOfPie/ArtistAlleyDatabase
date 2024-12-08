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

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.characters.data"
}
