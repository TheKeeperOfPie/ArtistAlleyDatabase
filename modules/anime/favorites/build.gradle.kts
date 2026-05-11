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
            implementation(projects.modules.utils)
            implementation(libs.stately.concurrent.collections)
        }
    }
}

kotlin {
    android {
        namespace = "com.thekeeperofpie.artistalleydatabase.anime.favorites"
    }
}
