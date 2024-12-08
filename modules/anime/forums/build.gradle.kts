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
            api(projects.modules.utilsCompose)
            implementation(projects.modules.anime.ui)
            implementation(projects.modules.anime.media.data)
            implementation(projects.modules.markdown)
            implementation(projects.modules.utils)
            implementation(libs.coil3.coil.compose)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.stately.concurrent.collections)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.forums"
}

compose.resources {
    publicResClass = true
}
