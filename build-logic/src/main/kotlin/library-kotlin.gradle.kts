@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
plugins {
    id("app.cash.burst")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    applyDefaultHierarchyTemplate()
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        jvmToolchain(18)
        freeCompilerArgs.addAll(
            listOf(
                "-Xcontext-parameters",
                "-Xexpect-actual-classes",
                "-Xjspecify-annotations=strict",
                "-Xtype-enhancement-improvements-strict-mode",
            )
        )
        optIn.addAll(
            listOf(
                "androidx.compose.foundation.ExperimentalFoundationApi",
                "androidx.compose.material3.ExperimentalMaterial3Api",
                "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
                "androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
                "androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi",
                "kotlin.js.ExperimentalWasmJsInterop",
                "kotlin.time.ExperimentalTime",
                "kotlin.uuid.ExperimentalUuidApi",
                "kotlinx.coroutines.ExperimentalCoroutinesApi",
                "kotlinx.serialization.ExperimentalSerializationApi",
            )
        )
    }

    sourceSets {
        commonMain.dependencies {
            libs.find(
                "libs.flowExt",
            ).forEach(::api)

            libs.find(
                "libs.androidx.annotation",
                "libs.kermit",
                "libs.kotlinx.coroutines.core",
                "libs.kotlinx.datetime",
                "libs.kotlinx.serialization.json",
            ).forEach(::implementation)
        }
        commonTest.dependencies {
            implementation(project(":modules:test-utils"))
            implementation(kotlin("test"))
            libs.find(
                "libs.kotlinx.coroutines.test",
                "libs.truth",
                "libs.turbine",
            ).forEach(::implementation)
        }
    }
}
