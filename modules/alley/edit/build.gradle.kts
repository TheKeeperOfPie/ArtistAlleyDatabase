plugins {
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-kotlin")
    id("library-web")
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.alley.edit"
    }
    sourceSets {
        commonMain.dependencies {
            api(libs.jetBrainsAndroidX.navigation3.ui)

            implementation(compose.components.resources)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.preview)
            implementation(compose.runtime)
            implementation(compose.ui)

            implementation(libs.coil3.coil.compose)
            implementation(libs.jetBrainsAndroidX.lifecycle.viewmodel.navigation3)
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(projects.modules.alley)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(projects.modules.utilsInject)
        }
    }
}
