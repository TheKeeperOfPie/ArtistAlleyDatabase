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
            api(projects.modules.anime.data)
            api(projects.modules.anime.ignore.data)
            api(projects.modules.anime.ui)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(libs.jetBrainsCompose.navigation.compose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.media.data"
}
