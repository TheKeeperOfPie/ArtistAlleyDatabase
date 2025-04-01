import org.gradle.kotlin.dsl.libs
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
            group("androidAndDesktop") {
                withAndroidTarget()
                withJvm()
            }
            group("desktopAndWasm") {
                withJvm()
                withWasmJs()
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            api(libs.paging.compose.android)
            implementation(libs.activity.compose)
            implementation(libs.html.text)
            implementation(libs.palette.ktx)
            runtimeOnly(libs.paging.runtime.ktx)
        }
        commonMain.dependencies {
            api(compose.components.resources)
            api(libs.compose.placeholder.material3)
            api(libs.jetBrainsCompose.ui.backhandler)
            api(libs.pagingMultiplatform.paging.common)

            implementation(libs.coil3.coil.compose)
            implementation(libs.colormath.ext.jetpack.compose)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
            implementation(libs.molecule.runtime)
            implementation(projects.modules.utils)
        }
        desktopMain.dependencies {
            api(libs.paging.common.jvm)
            implementation(libs.kmpalette.core)
        }
        wasmJsMain.dependencies {
            implementation(libs.pagingMultiplatform.paging.compose.common)
        }
        val androidAndDesktopMain by getting {
            dependencies {
                compileOnly(libs.paging.common.jvm)
            }
        }
        val desktopAndWasmMain by getting {
            dependencies {
                implementation(libs.human.readable)
            }
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils_compose"
}

compose.resources {
    publicResClass = true
}
