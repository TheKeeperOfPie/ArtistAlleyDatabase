plugins {
    id("library-android")
}

kotlin {
    jvm("desktop")
    // TODO: This target is required to resolve kotlin-test properly, but why?
    iosX64()

    sourceSets {
        commonMain.dependencies {
            api(libs.bignum)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils"
}
