plugins {
    id("library-android")
    id("library-desktop")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.io.core)
            api(libs.kotlinx.serialization.json.io)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils_room"
}

dependencies {
    add("kspAndroid", kspProcessors.room.compiler)
}
