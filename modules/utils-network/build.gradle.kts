import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
    id("library-web")
}

kotlin {
    sourceSets {
        val jvmMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.apollo.engine.ktor)
                implementation(libs.okhttp3.logging.interceptor)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.skrapeit)
            }
        }
        androidMain {
            dependsOn(jvmMain)
            dependencies {
                implementation(libs.cronet.okhttp)
                implementation(projects.modules.utilsBuildConfig)
            }
        }
        val desktopMain by getting {
            dependsOn(jvmMain)
        }
        commonMain.dependencies {
            api(libs.apollo.runtime)
            api(libs.ktor.client.core)
        }
        wasmJsMain.dependencies {
            implementation(libs.apollo.engine.ktor.wasm.js)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.utils_network"
    }
}
