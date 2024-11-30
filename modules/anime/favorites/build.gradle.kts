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
            implementation(projects.modules.utils)
            implementation(libs.stately.concurrent.collections)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime.favorites"
}
