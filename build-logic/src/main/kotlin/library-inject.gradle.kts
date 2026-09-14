@file:Suppress("UnstableApiUsage")

import dev.zacsweers.metro.gradle.DiagnosticSeverity
import dev.zacsweers.metro.gradle.RequiresIdeSupport

plugins {
    id("library-kotlin")
    id("library-desktop")
    id("dev.zacsweers.metro")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":modules:utils-inject"))
        }
    }
}

@OptIn(RequiresIdeSupport::class)
metro {
    generateAssistedFactories.set(true)
    generateContributionProviders.set(true)
    unusedGraphInputsSeverity.set(DiagnosticSeverity.ERROR)
    desugaredProviderSeverity.set(DiagnosticSeverity.ERROR)
}
