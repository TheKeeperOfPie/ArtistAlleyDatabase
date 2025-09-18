plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(libs.androidx.security.crypto)
            api(libs.work.runtime)
            api(libs.work.runtime.ktx)
        }
        commonMain.dependencies {
            api(projects.modules.anime)
            api(projects.modules.art)
            api(projects.modules.cds)
            api(projects.modules.monetization)
            api(projects.modules.secrets)
            implementation(projects.modules.anime.ignore.data)
            implementation(projects.modules.anime.media.data)
            implementation(projects.modules.anime.news)
            implementation(projects.modules.settings.ui)

            implementation(libs.coil3.coil.compose)
            implementation(libs.jetBrainsAndroidX.lifecycle.viewmodel.compose)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.settings"
    }
}

compose.resources {
    publicResClass = true
}
