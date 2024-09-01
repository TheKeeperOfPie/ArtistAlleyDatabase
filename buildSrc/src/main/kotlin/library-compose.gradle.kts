plugins {
    id("library-kotlin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
        }
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
