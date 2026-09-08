plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            runtimeOnly(libs.room.runtime)
            implementation(libs.room.paging)
        }
    }
}

compose.resources {
    publicResClass = true
}
