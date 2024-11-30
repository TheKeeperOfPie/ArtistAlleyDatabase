plugins {
    id("library-android")
    id("library-compose")
    id("library-inject")
    alias(libs.plugins.de.mannodermaus.android.junit5)
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

            implementation(libs.accompanist.flowlayout)
            implementation(libs.coil3.coil.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.molecule.runtime)
            implementation(libs.paging.common)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime2anime"
}
