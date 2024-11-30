plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-room")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.entry)
            api(projects.modules.utils)
            api(projects.modules.utilsCompose)
            api(projects.modules.utilsNetwork)

            implementation(libs.ksoup)
            implementation(libs.ktor.client.core)
            implementation(libs.okhttp)
        }
        commonTest.dependencies {
            implementation(libs.junit)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.skrapeit)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.vgmdb"
}

compose.resources {
    publicResClass = true
}
