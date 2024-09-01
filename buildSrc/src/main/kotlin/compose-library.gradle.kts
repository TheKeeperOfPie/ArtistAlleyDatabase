plugins {
    id("module-library")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    buildFeatures {
        compose = true
    }
}

composeCompiler {
    enableStrongSkippingMode = true
//    featureFlags = setOf(
//        ComposeFeatureFlag.StrongSkipping,
//        ComposeFeatureFlag.OptimizeNonSkippingGroups,
//    )
    includeSourceInformation = true
}
