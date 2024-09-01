plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.bignum)
            api(libs.kotlinx.io.core)
            api(libs.uri.kmp)
            implementation(libs.kermit)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils"
}
