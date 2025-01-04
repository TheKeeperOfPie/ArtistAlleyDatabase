plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
    id("library-web")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(libs.androidx.security.crypto)
        }
        commonMain.dependencies {
            api(libs.bignum)
            api(libs.kotlinx.io.core)
            api(libs.uri.kmp)
            implementation(libs.kotlinx.serialization.json.io)
        }
        desktopMain.dependencies {
            implementation(libs.jimfs)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils"

    buildFeatures {
        buildConfig = true
    }
}
