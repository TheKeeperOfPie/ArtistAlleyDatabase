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
            api(projects.modules.anime.ignore.data)
            api(projects.modules.anime.media.data)
            api(projects.modules.anime.search.data)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.seasonal"
}

compose.resources {
    publicResClass = true
}
