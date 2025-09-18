import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-web")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("web") {
                withJs()
                withWasmJs()
            }
        }
    }
    sourceSets {
        androidMain.dependencies {
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.uri.kmp)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.image"
    }
}
