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
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)

            implementation(libs.coil3.coil.compose)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.kermit)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.viewmodel.savedstate)
            implementation(libs.stately.concurrent.collections)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.recommendations"
}

compose.resources {
    publicResClass = true
}
