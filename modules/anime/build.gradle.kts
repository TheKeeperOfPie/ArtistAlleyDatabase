plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-room")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.anime.activities)
            api(projects.modules.anime.characters)
            api(projects.modules.anime.favorites)
            api(projects.modules.anime.forums)
            api(projects.modules.anime.history)
            api(projects.modules.anime.ignore)
            api(projects.modules.anime.media.data)
            api(projects.modules.anime.news)
            api(projects.modules.anime.notifications)
            api(projects.modules.anime.recommendations)
            api(projects.modules.anime.reviews)
            api(projects.modules.anime.schedule)
            api(projects.modules.anime.search)
            api(projects.modules.anime.seasonal)
            api(projects.modules.anime.songs)
            api(projects.modules.anime.staff)
            api(projects.modules.anime.studios)
            api(projects.modules.anime.studios.data)
            api(projects.modules.anime.users)

            implementation(projects.modules.anilist)
            implementation(projects.modules.anime.data)
            implementation(projects.modules.anime.ui)
            implementation(projects.modules.cds)
            implementation(projects.modules.markdown)
            implementation(projects.modules.media)
            implementation(projects.modules.monetization)
            implementation(projects.modules.utilsCompose)
            implementation(projects.modules.utilsNetwork)

            implementation(libs.coil3.coil.compose)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.fluid.country)
            implementation(libs.fluid.i18n)
            implementation(libs.htmlconverter)
            implementation(libs.human.readable)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
            implementation(libs.kotlinx.serialization.json.io)
            implementation(libs.stately.concurrent.collections)
        }
        commonTest.dependencies {
            implementation(projects.modules.anime.ignore.testing)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.anime"
    }
}

compose.resources {
    publicResClass = true
}
