@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("library-kotlin")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        androidResources {
            enable = true // Required for CMP resources to work, probably a bug?
        }
    }
    androidLibrary {
        compileSdk = 36
        minSdk = 28

        compilerOptions {
            jvmTarget = JvmTarget.JVM_18
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget = JvmTarget.JVM_18
                }
            }
        }
    }

    sourceSets {
        getByName("androidDeviceTest").dependencies {
            implementation(project(":modules:test-utils"))
            implementation(project(":modules:utils-network"))
            resolveLibraries("libs.kotlinx.coroutines.test")
                .forEach(::implementation)
        }
    }
}
