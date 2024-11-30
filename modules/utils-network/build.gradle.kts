plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.cronet.okhttp)
        }
        commonMain.dependencies {
            api(libs.apollo.runtime)
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.okhttp3.logging.interceptor)
            implementation(libs.skrapeit)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils_network"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    buildFeatures {
        buildConfig = true
    }
}
