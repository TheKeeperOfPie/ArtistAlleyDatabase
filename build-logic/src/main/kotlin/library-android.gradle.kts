@file:Suppress("UnstableApiUsage")

import gradle.kotlin.dsl.accessors._90bd174187cd08138b72b189c4e3a8fa.android
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("library-kotlin")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        val subpackage = project.path.split(":").joinToString(".")
        namespace = "com.thekeeperofpie.artistalleydatabase.$subpackage"

        compileSdk = 37
        minSdk = 28

        androidResources {
            enable = true // Required for CMP resources to work, probably a bug?
        }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_18)
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
