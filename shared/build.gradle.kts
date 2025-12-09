
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.3.0-RC2"
    id("com.android.library") version "8.13.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0-RC2"
}

group = "com.thekeeperofpie.artistalleydatabase.shared"
version = "0.0.1"

kotlin {
    compilerOptions {
        jvmToolchain(18)
    }

    androidTarget {
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

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.shared"
    compileSdk = 36
    defaultConfig { minSdk = 28 }
}
