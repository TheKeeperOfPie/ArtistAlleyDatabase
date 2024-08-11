@file:Suppress("UnstableApiUsage")

import gradle.kotlin.dsl.accessors._b2937d1b40dda98f7678619569c6e850.kotlin
import gradle.kotlin.dsl.accessors._b2937d1b40dda98f7678619569c6e850.sourceSets
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        jvmToolchain(18)
        sourceSets.all {
            languageSettings {
                languageSettings.optIn("kotlin.RequiresOptIn")
            }
        }
        freeCompilerArgs.add("-Xcontext-receivers")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            libs.find(
                "libs.androidx.annotation",
                "libs.kotlinx.coroutines.core",
            ).forEach(::implementation)
        }
    }
}
