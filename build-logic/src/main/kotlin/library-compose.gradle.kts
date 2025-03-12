@file:OptIn(ExperimentalComposeLibrary::class)

import gradle.kotlin.dsl.accessors._9e1c1cd6318aab2d8fc99bbe0d3fcd08.debugImplementation
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

dependencies {
    debugImplementation(compose.uiTooling)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
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
