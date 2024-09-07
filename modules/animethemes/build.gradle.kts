plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":modules:anime"))
            api(project(":modules:utils-network"))
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json.io)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.animethemes"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}
