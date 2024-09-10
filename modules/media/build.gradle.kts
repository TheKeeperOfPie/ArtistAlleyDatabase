plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.media3.datasource.okhttp)
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.exoplayer.dash)
            implementation(libs.media3.exoplayer.hls)
            implementation(libs.media3.exoplayer.rtsp)
            implementation(libs.media3.ui)
            implementation(libs.androidyoutubeplayer)
        }
        commonMain.dependencies {
            api(libs.bignum)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.media"
}
