import dev.zacsweers.metro.gradle.DelicateMetroGradleApi

plugins {
    id("library-android")
    id("library-desktop")
    id("library-web")
    alias(libs.plugins.dev.zacsweers.metro)
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.inject"
    }
}

@OptIn(DelicateMetroGradleApi::class)
metro {
    enableTopLevelFunctionInjection.set(false)
    generateContributionHintsInFir.set(false)
    supportedHintContributionPlatforms.set(emptySet())
}
