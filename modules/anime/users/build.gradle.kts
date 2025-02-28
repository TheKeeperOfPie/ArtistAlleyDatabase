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
            api(projects.modules.anime.characters.data)
            api(projects.modules.anime.ignore.data)
            api(projects.modules.anime.media.data)
            api(projects.modules.anime.staff.data)
            api(projects.modules.anime.studios.data)
            api(projects.modules.anime.users.data)
            api(projects.modules.markdown)
            implementation(projects.modules.anime.ui)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)

            implementation(libs.bignum)
            implementation(libs.coil3.coil.compose)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.users"
}

compose.resources {
    publicResClass = true
}
