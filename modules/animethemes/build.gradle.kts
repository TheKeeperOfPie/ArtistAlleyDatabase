plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.anime)
            api(projects.modules.utilsNetwork)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json.io)
            implementation(libs.kermit)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.animethemes"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}
