@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.3.0"
    id("com.android.kotlin.multiplatform.library") version "9.0.0-rc01"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
}

group = "com.thekeeperofpie.artistalleydatabase.shared"
version = "0.0.1"

kotlin {
    compilerOptions {
        jvmToolchain(18)
    }

    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.shared"
        compileSdk { version = release(36) }
        minSdk = 28
        compilerOptions {
            jvmTarget = JvmTarget.JVM_18
        }
    }
    jvm()
    js {
        browser {
            commonWebpackConfig {
                sourceMaps = false
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                sourceMaps = false
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.uri.kmp)
            }
        }
    }
}
