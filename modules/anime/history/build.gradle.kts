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
            api(projects.modules.anime.ignore.data)
            api(projects.modules.anime.media.data)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.anime.history"
    }
}
