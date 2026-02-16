@file:Suppress("UnstableApiUsage")

import dev.zacsweers.metro.gradle.DelicateMetroGradleApi


val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
val Project.kspProcessors: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("kspProcessors")
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

@OptIn(DelicateMetroGradleApi::class)
metro {
    // https://github.com/ZacSweers/metro/releases/tag/0.10.3
    enableTopLevelFunctionInjection.set(false)
    generateContributionHintsInFir.set(false)
    supportedHintContributionPlatforms.set(emptySet())

    generateAssistedFactories.set(true)
}
