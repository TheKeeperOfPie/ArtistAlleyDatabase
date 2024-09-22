plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.uri.kmp)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.image"
}
