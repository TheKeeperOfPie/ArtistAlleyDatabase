import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-web")
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("desktopAndWeb") {
                withJvm()
                withJs()
                withWasmJs()
            }
            group("web") {
                withJs()
                withWasmJs()
            }
        }
    }

    sourceSets {
        val androidAndDesktopMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain {
            dependsOn(androidAndDesktopMain)
            dependencies {
                implementation(libs.activity.compose)
                implementation(libs.html.text)
                implementation(libs.palette.ktx)
                runtimeOnly(libs.paging.runtime.ktx)
            }
        }
        commonMain.dependencies {
            api(compose.components.resources)
            api(libs.compose.placeholder.material3)
            api(libs.jetBrainsAndroidX.lifecycle.runtime.compose)
            api(libs.jetBrainsAndroidX.lifecycle.viewmodel.compose)
            api(libs.jetBrainsCompose.ui.backhandler)
            api(libs.paging.common)
            api(libs.paging.compose)

            implementation(libs.coil3.coil.compose)
            implementation(libs.colormath.ext.jetpack.compose)
            implementation(libs.jetBrainsAndroidX.lifecycle.runtime.compose)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
            implementation(libs.molecule.runtime)
            implementation(projects.modules.utils)
        }
        desktopMain {
            dependsOn(androidAndDesktopMain)
            dependencies {
                implementation(libs.kmpalette.core)
            }
        }
        val desktopAndWebMain by getting {
            dependencies {
                implementation(libs.human.readable)
            }
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.utils_compose"
    }
}

compose.resources {
    publicResClass = true
}
