plugins {
    id("library-android")
    id("library-desktop")
}

kotlin {
    // TODO: This target is required to resolve kotlin-test properly, but why?
    iosX64()

    sourceSets {
        commonMain.dependencies {
            api(libs.bignum)
            api(libs.kotlinx.io.core)
            api(libs.uri.kmp)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils"
}
