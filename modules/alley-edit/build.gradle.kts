import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.dev.zacsweers.metro)
}

compose.desktop {
    application {
        mainClass = "com.thekeeperofpie.artistalleydatabase.alley.edit.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "com.thekeeperofpie.artistalleydatabase.alley.edit"
            packageVersion = "0.0.1"
        }
    }
}

kotlin {
    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate()

    compilerOptions {
        jvmToolchain(18)
        freeCompilerArgs.add("-Xcontext-receivers")
        freeCompilerArgs.add("-Xwasm-use-new-exception-proposal")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)

            implementation(libs.coil3.coil.compose)
            implementation(projects.modules.alley)
            implementation(projects.modules.alley.edit)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

metro {
    generateAssistedFactories.set(true)
}

configurations.all {
    resolutionStrategy {
        capabilitiesResolution.withCapability("com.google.guava:listenablefuture") {
            select("com.google.guava:guava:0")
        }
        // com.eygraber:uri-kmp:0.0.19 bumps this to 0.3, which breaks CMP
        force("org.jetbrains.kotlinx:kotlinx-browser:0.1")
    }
}

val distribution: NamedDomainObjectProvider<Configuration> by configurations.registering {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(distribution.name, tasks.named("wasmJsBrowserDistribution"))
}
