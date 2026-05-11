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
            api(projects.modules.anilist.data)
            api(projects.modules.anime.data)
            api(projects.modules.anime.characters.data)
            api(projects.modules.anime.staff.data)
            api(projects.modules.markdown)

            implementation(projects.modules.anime.media.data)
            implementation(projects.modules.icons)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(libs.coil3.coil.compose)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
        }
    }
}

kotlin {
    android {
        namespace = "com.thekeeperofpie.artistalleydatabase.anime.staff"
    }
}

compose.resources {
    publicResClass = true
}
