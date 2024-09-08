plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.monetization"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(libs.activity.compose)
        }
        commonMain.dependencies {
            api(project(":modules:utils"))
            api(project(":modules:utils-compose"))
            implementation(libs.lifecycle.viewmodel.compose)
        }
    }
}
