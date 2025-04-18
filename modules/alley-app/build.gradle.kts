
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.util.zip.CRC32

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
    compileSdk = 36

    defaultConfig {
        applicationId = "com.thekeeperofpie.artistalley2025"
        minSdk = 28
        targetSdk = 36
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
        outputModuleName.set("ArtistAlley")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = null
                mode = KotlinWebpackConfig.Mode.PRODUCTION
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
//        freeCompilerArgs.add("-Xwasm-use-new-exception-proposal")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(libs.coil3.coil.network.ktor3)
            implementation(libs.kotlinx.coroutines.core)
        }
        val nonJsCommonMain by getting {
            dependencies {
                implementation(projects.modules.alley)
                implementation(projects.modules.utils)
                implementation(projects.modules.utilsCompose)
                implementation(projects.modules.utilsInject)

                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)

                implementation(libs.kotlinx.serialization.json)

                implementation(libs.coil3.coil.compose)
                implementation(libs.jetBrainsAndroidX.navigation.compose)
                implementation(libs.kermit)
                implementation(libs.kotlin.inject.runtime.kmp)
            }
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            runtimeOnly(libs.kotlinx.coroutines.android)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.java)
            }
        }
        val wasmJsMain by getting {
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

tasks.getByPath("preBuild").dependsOn(":copyGitHooks")

tasks.named { "wasmJsProcessResources" in it }.configureEach {
    dependsOn("serviceWorkerBrowserDistribution")
    // Ignore duplicate composeResources included from JS :modules:alley:data dependency
    (this as ProcessResources).duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

configurations.all {
    resolutionStrategy {
        capabilitiesResolution.withCapability("com.google.guava:listenablefuture") {
            select("com.google.guava:guava:0")
        }
        // com.eygraber:uri-kmp:0.0.19 bumps this to 0.3, which breaks CMP
        force("org.jetbrains.kotlinx:kotlinx-browser:0.1")
        force("org.jetbrains.compose.material3:material3:1.8.0-alpha03")
    }
}

// Replicates Workbox InjectManifest since configuring that doesn't seem to work
tasks.register("webRelease") {
    outputs.upToDateWhen { false }
    dependsOn(":modules:alley:user:verifySqlDelightMigration", "wasmJsBrowserDistribution")

    val distDir = project.layout.buildDirectory.dir("dist/wasmJs/productionExecutable")
    doLast {
        val folder = distDir.get().asFile
        folder.listFiles()
            .filter { it.extension == "map" }
            .forEach { it.delete() }
        val serviceWorker = folder.resolve("serviceWorker.js")
        val rootFiles = folder.listFiles()
            .filter { it.isFile }
            .filter { it.name != "serviceWorker.js" }
            .filter { it.name != "_headers" }

        val icons = folder.resolve("icons")
            .listFiles()

        val resourceFiles = folder.resolve("composeResources")
            .walkBottomUp()
            .filter { it.isFile }
            .filter { it.extension == "cvr" || it.name.contains("database") }

        val filesToCache = rootFiles + icons + resourceFiles

        fun hash(file: File): Long {
            val crc32 = CRC32()
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    crc32.update(buffer, 0, bytesRead)
                }
            }
            return crc32.value
        }

        val fileNamesAndHashes = filesToCache
            .joinToString(separator = "\\n") {
                val relativePath = it.relativeTo(folder).path.replace(File.separatorChar, '/')
                "$relativePath-${hash(it)}"
            }
        val serviceWorkerEdited = serviceWorker.readText()
            .replace("CACHE_INPUT", fileNamesAndHashes)
        serviceWorker.writeText(serviceWorkerEdited)
    }
}
