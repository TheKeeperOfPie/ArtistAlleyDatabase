plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.browse"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":modules:entry"))
            implementation(project(":modules:utils"))
            implementation(project(":modules:utils-compose"))
            implementation(libs.jetBrainsCompose.navigation.compose)
        }
    }
}
