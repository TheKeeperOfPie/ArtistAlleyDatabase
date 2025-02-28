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
            api(projects.modules.anime.studios.data)
            api(projects.modules.utilsCompose)
            implementation(projects.modules.utils)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.studios"
}
