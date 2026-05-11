plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-web")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.modules.utilsCompose)
        }
    }
}

kotlin {
    android {
        namespace = "com.thekeeperofpie.artistalleydatabase.settings.ui"
    }
}
