@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag

plugins {
    id("library-android")
    id("library-desktop")
    id("library-kotlin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation("org.jetbrains.compose.material3:material3:1.10.0-alpha04")
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
            }
        }
        commonTest.dependencies {
            implementation(compose.uiTest)
        }
        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.uiTooling)
            }
        }
    }
}

composeCompiler {
    featureFlags = setOf(
        ComposeFeatureFlag.StrongSkipping,
        ComposeFeatureFlag.OptimizeNonSkippingGroups,
    )
    includeSourceInformation = true
}
