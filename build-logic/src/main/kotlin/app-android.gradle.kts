@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget = JvmTarget.JVM_18
        }
    }
}

