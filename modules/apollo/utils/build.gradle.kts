plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.apollo.runtime)
            implementation(projects.modules.utils)
            implementation(libs.apollo.normalized.cache)
            implementation(libs.kermit)
            implementation(libs.kotlinx.serialization.json.io)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.apollo.utils"
}
