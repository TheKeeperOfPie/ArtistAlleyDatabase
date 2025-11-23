plugins {
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-kotlin")
    id("library-wasmJs")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.jetBrainsAndroidX.navigation3.ui)

            implementation(compose.components.resources)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(compose.materialIconsExtended)
            implementation(compose.preview)
            implementation(compose.runtime)
            implementation(compose.ui)

            implementation(libs.coil3.coil.compose)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.jetBrainsAndroidX.lifecycle.viewmodel.navigation3)
            implementation(libs.jetBrainsCompose.material3.windowSizeClass)
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.json.io)
            implementation(libs.stately.concurrent.collections)

            implementation(projects.modules.alley)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(projects.modules.utilsInject)
            implementation(projects.modules.utilsNetwork)
        }
    }
}
