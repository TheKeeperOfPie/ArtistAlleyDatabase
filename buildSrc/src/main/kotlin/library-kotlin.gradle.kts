@file:Suppress("UnstableApiUsage")

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

    sourceSets.getByName("commonMain").dependencies {
        libs.find(
            "libs.flowExt",
        ).forEach(::api)

        libs.find(
            "libs.androidx.annotation",
            "libs.kotlinx.coroutines.core",
            "libs.kotlinx.datetime",
            "libs.kotlinx.serialization.json",
        ).forEach(::implementation)
    }
}
