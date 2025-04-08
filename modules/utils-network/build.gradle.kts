import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
    id("library-web")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvm") {
                withAndroidTarget()
                withJvm()
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.cronet.okhttp)
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.apollo.engine.ktor)
                implementation(libs.okhttp3.logging.interceptor)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.skrapeit)
            }
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

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils_network"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    buildFeatures {
        buildConfig = true
    }
}
