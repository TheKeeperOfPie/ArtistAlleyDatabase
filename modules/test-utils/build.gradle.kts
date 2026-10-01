plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-web")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.google.truth)
            implementation(kotlin("test"))
            implementation(projects.modules.utils)
            implementation(libs.turbine)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
