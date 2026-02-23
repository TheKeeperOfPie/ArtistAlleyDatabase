@file:Suppress("UnstableApiUsage")

import dev.zacsweers.metro.gradle.DelicateMetroGradleApi
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("androidx.room")
    alias(libs.plugins.dev.zacsweers.metro)
    alias(libs.plugins.com.github.ben.manes.versions)
}

compose{
    resources {
        publicResClass = true
    }
    desktop {
        application {
            mainClass = "com.thekeeperofpie.artistalleydatabase.desktop.MainKt"

            nativeDistributions {
                targetFormats(TargetFormat.Exe)
                packageName = "com.thekeeperofpie.artistalleydatabase"
                packageVersion = "0.0.1"
            }
        }
    }
}

@OptIn(DelicateMetroGradleApi::class)
metro {
    enableTopLevelFunctionInjection.set(false)
    generateContributionHintsInFir.set(false)
    supportedHintContributionPlatforms.set(emptySet())
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase"
        compileSdk {
            version = release(36)
        }
        androidResources { enable = true }
    }
    jvm("desktop")
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        optIn.add("kotlin.time.ExperimentalTime")
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    sourceSets {
        commonMain.dependencies {
            implementation(projects.modules.anime)
            implementation(projects.modules.art)
            implementation(projects.modules.media)
            implementation(projects.modules.monetization)
            implementation(projects.modules.settings)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsInject)
            implementation(projects.modules.utilsNetwork)
            implementation(projects.modules.utilsRoom)

            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)

            implementation(libs.coil3.coil.compose)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
            implementation(libs.kermit)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.androidx.sqlite.bundled)
                implementation(libs.coil3.coil.network.ktor3)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.uri.kmp)
            }
        }
    }
}

dependencies {
    arrayOf(
        kspProcessors.room.compiler,
    ).forEach {
        add("kspAndroid", it)
        add("kspDesktop", it)
    }
}

room {
    schemaDirectory("$projectDir/schemas")
    generateKotlin = true
}

tasks.named { it.contains("explodeCodeSource") }.configureEach {
    dependsOn("generateResourceAccessorsForAndroidMain")
    dependsOn("generateActualResourceCollectorsForAndroidMain")
}

configurations.all {
    resolutionStrategy {
        capabilitiesResolution.withCapability("com.google.guava:listenablefuture") {
            select("com.google.guava:guava:0")
        }

        // https://github.com/Kotlin/kotlinx.serialization/issues/2968#issuecomment-3356075918
        eachDependency {
            if (requested.module.group == "org.jetbrains.kotlinx" &&
                requested.module.name.startsWith("kotlinx-serialization")
            ) {
                useVersion("1.9.0")
            }
        }
    }
}
