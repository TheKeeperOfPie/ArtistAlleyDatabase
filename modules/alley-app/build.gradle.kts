
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
}

android {
    namespace = "com.thekeeperofpie.artistalley"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thekeeperofpie.artistalley2024"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resourceConfigurations += "en"
    }

    val proguardFiles = (file("proguard/").listFiles().orEmpty().toList() +
            getDefaultProguardFile("proguard-android-optimize.txt")).toTypedArray()

    val debugKeystore = file(System.getProperty("user.home"))
        .resolve(".android")
        .resolve("debug.keystore")
    val debugKeystoreExists = debugKeystore.exists()

    if (debugKeystoreExists) {
        signingConfigs {
            create("default") {
                keyAlias = "androiddebugkey"
                keyPassword = "android"
                storeFile = debugKeystore
                storePassword = "android"
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            isCrunchPngs = false
            proguardFiles(*proguardFiles)

            if (debugKeystoreExists) {
                signingConfig = signingConfigs.getByName("default")
            }
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            proguardFiles(*proguardFiles)

            if (debugKeystoreExists) {
                signingConfig = signingConfigs.getByName("default")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            merges += "/META-INF/*"
            merges += "mozilla/public-suffix-list.txt"

            // Can happen if an archive was built incrementally and accidentally published as-is
            excludes += "**/previous-compilation-data.bin"
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.thekeeperofpie.artistalleydatabase.alley.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "com.thekeeperofpie.artistalley"
            packageVersion = "0.0.1"
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_18
        }
    }
    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "ArtistAlley"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = devServer ?: KotlinWebpackConfig.DevServer()
            }
        }
        binaries.executable()
    }

    js("serviceWorker", KotlinJsCompilerType.IR) {
        binaries.executable()
        browser {
            webpackTask {
                mainOutputFileName = "serviceWorker.js"
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("nonJsCommon") {
                withAndroidTarget()
                withJvm()
                withWasmJs()
            }
        }
    }

    compilerOptions {
        jvmToolchain(18)
        sourceSets.all {
            languageSettings {
                languageSettings.optIn("kotlin.RequiresOptIn")
            }
        }
        freeCompilerArgs.add("-Xcontext-receivers")
        freeCompilerArgs.add("-Xwasm-use-new-exception-proposal")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(libs.kotlinx.coroutines.core)
        }
        val nonJsCommonMain by getting {
            dependencies {
                implementation(projects.modules.alley)
                implementation(projects.modules.utils)
                implementation(projects.modules.utilsCompose)
                implementation(projects.modules.utilsInject)

                implementation(compose.components.uiToolingPreview)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)

                implementation(libs.kotlinx.serialization.json)

                implementation(libs.coil3.coil.compose)
                implementation(libs.jetBrainsCompose.navigation.compose)
                implementation(libs.kermit)
                implementation(libs.kotlin.inject.runtime.kmp)
            }
        }
        androidMain.dependencies {
            runtimeOnly(libs.kotlinx.coroutines.android)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.coil3.coil.network.ktor3)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.okio.fakefilesystem)
            }
            resources.srcDirs(layout.buildDirectory.dir("dist/serviceWorker/productionExecutable"))
        }
        val serviceWorkerMain by getting {
            dependencies {
                implementation(projects.modules.alley.data)
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", kspProcessors.kotlin.inject.compiler.ksp)
    add("kspAndroid", kspProcessors.kotlin.inject.compiler.ksp)
    add("kspDesktop", kspProcessors.kotlin.inject.compiler.ksp)
    add("kspWasmJs", kspProcessors.kotlin.inject.compiler.ksp)
}

tasks.register("installAll") {
    dependsOn("installDebug", "installRelease")
}

fun Exec.launchActivity(
    packageName: String,
    activityName: String = "com.thekeeperofpie.artistalley.MainActivity"
) {
    commandLine(
        "adb", "shell", "am", "start-activity",
        "-a", "\"android.intent.action.MAIN\"",
        "-c", "\"android.intent.category.LAUNCHER\"",
        "-n", "\"$packageName/$activityName\"",
    )
}

tasks.register("launchRelease") {
    dependsOn("installRelease")
    finalizedBy("compileAndLaunchRelease", "installDebug")
    outputs.upToDateWhen { false }
}

tasks.register<Exec>("launchDebug") {
    dependsOn("installDebug")
    launchActivity("com.thekeeperofpie.artistalley.debug")
    finalizedBy("installRelease")
    outputs.upToDateWhen { false }
}

tasks.register<Exec>("compileAndLaunchRelease") {
    commandLine(
        "adb", "shell", "pm", "compile", "-f",
        "-m", "everything",
        "--check-prof", "false",
        "com.thekeeperofpie.artistalley",
    )
    finalizedBy("launchReleaseMainActivity")
    outputs.upToDateWhen { false }
}

tasks.register<Exec>("launchReleaseMainActivity") {
    launchActivity("com.thekeeperofpie.artistalley")
    outputs.upToDateWhen { false }
}

tasks.getByPath("preBuild").dependsOn(":copyGitHooks")

tasks.named { "wasmJsProcessResources" in it }.configureEach {
    dependsOn("serviceWorkerBrowserDistribution")
    // Ignore duplicate composeResources included from JS :modules:alley:data dependency
    (this as ProcessResources).duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

configurations.all {
    resolutionStrategy{
        capabilitiesResolution.withCapability("com.google.guava:listenablefuture") {
            select("com.google.guava:guava:0")
        }
        // com.eygraber:uri-kmp:0.0.19 bumps this to 0.3, which breaks CMP
        force("org.jetbrains.kotlinx:kotlinx-browser:0.1")
    }
}
