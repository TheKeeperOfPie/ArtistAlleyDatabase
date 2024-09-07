plugins {
    id("library-android")
    id("library-desktop")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.bignum)
            api(libs.kotlinx.io.core)
            api(libs.kotlinx.serialization.json.io)
            api(libs.room.ktx)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils_room"
}

dependencies {
    add("kspAndroid", kspProcessors.room.compiler)
}
