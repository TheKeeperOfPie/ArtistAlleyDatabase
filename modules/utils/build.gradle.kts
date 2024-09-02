plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.security.crypto)
        }
        commonMain.dependencies {
            api(libs.bignum)
            api(libs.kotlinx.io.core)
            api(libs.uri.kmp)
            implementation(libs.kermit)
            implementation(libs.kotlinx.serialization.json.io)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils"

    buildFeatures {
        buildConfig = true
    }
}
