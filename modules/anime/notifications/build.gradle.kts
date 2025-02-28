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
            api(projects.modules.anime.activities.data)
            api(projects.modules.anime.forums.data)
            api(projects.modules.anime.media.data)
            api(projects.modules.anime.ui)
            api(projects.modules.utilsCompose)

            implementation(projects.modules.utils)
            implementation(libs.coil3.coil.compose)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.human.readable)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.notifications"
}

compose.resources {
    publicResClass = true
}
