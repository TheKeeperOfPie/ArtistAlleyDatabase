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
            api(projects.modules.anime.media.data)
            implementation(projects.modules.anime.ui)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(libs.coil3.coil.compose)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.human.readable)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
            implementation(libs.jetBrainsAndroidX.lifecycle.viewmodel.compose)
            implementation(libs.stately.concurrent.collections)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.activities"
}

compose.resources {
    publicResClass = true
}
