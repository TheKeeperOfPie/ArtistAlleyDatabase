@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag

plugins {
    id("library-desktop")
    id("library-kotlin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

val composeFiles = project.layout.projectDirectory.dir("src/commonMain/composeResources/files").asFile
if (composeFiles.exists()) {
    tasks.register<ComposeFilesParseTask>("parseComposeFiles")
    tasks.named { it == "preBuild" }.first().dependsOn("parseComposeFiles")
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(project.layout.buildDirectory.dir("generated/source"))
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
