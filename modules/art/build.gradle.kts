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
            api(projects.modules.anilist)
            api(projects.modules.browse)
            api(projects.modules.data)
            api(projects.modules.entry)
            implementation(projects.modules.utilsCompose)
            implementation(projects.modules.utilsRoom)

            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.coil3.coil.compose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.art"
}

compose.resources {
    publicResClass = true
}
