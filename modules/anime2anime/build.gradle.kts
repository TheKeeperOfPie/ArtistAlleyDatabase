plugins {
    id("library-android")
    id("library-compose")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.modules.anilist)
            implementation(projects.modules.anime)
            implementation(projects.modules.anime.ignore.data)
            implementation(projects.modules.anime.media.data)
            implementation(projects.modules.anime.news)
            implementation(projects.modules.anime.recommendations)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)

            implementation(libs.coil3.coil.compose)
            implementation(libs.jetBrainsAndroidX.lifecycle.viewmodel.compose)
            implementation(libs.molecule.runtime)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.anime2anime"
    }
}
