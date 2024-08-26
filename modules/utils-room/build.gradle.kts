plugins {
    id("library-android")
    id("com.google.devtools.ksp")
}

kotlin {
    iosX64()
    sourceSets {
        androidMain.dependencies {
            api(libs.room.ktx)
            api(libs.room.paging)
            runtimeOnly(libs.room.runtime)
        }
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
