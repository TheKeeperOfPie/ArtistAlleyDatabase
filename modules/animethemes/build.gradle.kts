import com.android.build.api.dsl.androidLibrary

plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.modules.anilist)
            implementation(projects.modules.anime.songs)
            implementation(projects.modules.utilsNetwork)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json.io)
        }
    }
}

kotlin{
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.animethemes"
    }
}
