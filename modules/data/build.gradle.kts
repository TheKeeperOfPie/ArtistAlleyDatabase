plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.data"
    }
}

kotlin{
    sourceSets{
        commonMain.dependencies {
            api(projects.modules.anilist)
            api(projects.modules.entry)
            api(projects.modules.utils)
        }
    }
}
