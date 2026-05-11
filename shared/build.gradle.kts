@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.3.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21"
    id("com.android.kotlin.multiplatform.library") version "9.2.1"
}

group = "com.thekeeperofpie.artistalleydatabase.shared"
version = "0.0.1"

kotlin {
    android {
        namespace = "com.thekeeperofpie.artistalleydatabase.shared"
        compileSdk = 36
        minSdk = 28
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_18)
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
                implementation(libs.androidx.annotation)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.uri.kmp)
            }
        }
    }
}
