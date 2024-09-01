plugins {
    id("library-android")
    id("library-desktop")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.cronet.okhttp)
        }
        commonMain.dependencies {
            api(libs.apollo.runtime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kermit)
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
