import dev.zacsweers.metro.gradle.DelicateMetroGradleApi

plugins {
    id("library-android")
    id("library-desktop")
    id("library-web")
    alias(libs.plugins.dev.zacsweers.metro)
}

@OptIn(DelicateMetroGradleApi::class)
metro {
    enableTopLevelFunctionInjection.set(false)
    generateContributionHintsInFir.set(false)
    supportedHintContributionPlatforms.set(emptySet())
}
