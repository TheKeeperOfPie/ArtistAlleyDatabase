@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary

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
            api(projects.modules.image)
            implementation(projects.modules.markdown)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)

            implementation(libs.coil3.coil.compose)
            implementation(libs.flowExt)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
            implementation(libs.kotlin.multiplatform.diff)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.uri.kmp)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.entry"
}

compose.resources {
    publicResClass = true
}
