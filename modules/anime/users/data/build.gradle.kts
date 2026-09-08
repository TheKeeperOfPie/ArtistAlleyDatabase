plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.anilist)
            api(projects.modules.anilist.data)
            api(projects.modules.utilsCompose)
        }
    }
}

compose.resources {
    publicResClass = true
}
