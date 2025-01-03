@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.image)
            implementation(projects.modules.markdown)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(projects.modules.utilsRoom)

            implementation(libs.coil3.coil.compose)
            implementation(libs.flowExt)
            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.kotlin.multiplatform.diff)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.uri.kmp)
            implementation(libs.uuid)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.entry"
}

compose.resources {
    publicResClass = true
}
