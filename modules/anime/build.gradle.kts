plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-room")
    alias(libs.plugins.de.mannodermaus.android.junit5)
}

kotlin {
    sourceSets {
        androidInstrumentedTest.dependencies {
            implementation(libs.junit.jupiter.params)
            implementation(libs.junit5.android.test.compose)
        }
        commonMain.dependencies {
            api(projects.modules.anime.favorites)
            api(projects.modules.anime.ignore.data)
            api(projects.modules.anime.media.data)
            api(projects.modules.anime.news)
            api(projects.modules.anime.recommendations)

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
            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.kermit)
            implementation(libs.kotlinx.serialization.json.io)
            implementation(libs.stately.concurrent.collections)
        }
        commonTest.dependencies {
            implementation(projects.modules.anime.ignore.testing)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime"
}

compose.resources {
    publicResClass = true
}
