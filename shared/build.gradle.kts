import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.1.20"
    id("com.android.library") version "8.11.0-alpha06"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20"
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
    js { browser() }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.shared"
    compileSdk = 36
    defaultConfig { minSdk = 28 }
}
