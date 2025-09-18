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
            implementation(projects.modules.anime.ui)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(libs.coil3.coil.compose)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
            implementation(libs.jetBrainsAndroidX.lifecycle.viewmodel.compose)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.anime.characters"
    }
}

compose.resources {
    publicResClass = true
}
