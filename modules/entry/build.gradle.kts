plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    alias(libs.plugins.de.mannodermaus.android.junit5)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":modules:image"))
            implementation(project(":modules:utils"))
            implementation(project(":modules:utils-compose"))
            implementation(project(":modules:utils-room"))

            implementation(libs.flowExt)

            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.coil3.coil.compose)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.uuid)
            implementation(libs.uri.kmp)
            implementation(libs.kotlin.multiplatform.diff)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.entry"
}

compose.resources {
    publicResClass = true
}
