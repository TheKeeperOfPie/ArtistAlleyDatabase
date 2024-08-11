plugins {
    id("module-library-multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.bignum)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils"
}
