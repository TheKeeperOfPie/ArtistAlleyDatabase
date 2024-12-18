@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("app-android")
}

android {
    namespace = "com.thekeeperofpie.anichive"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thekeeperofpie.anichive"
        minSdk = 28
        targetSdk = 34
        versionCode = 15
        versionName = "0.50"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    androidResources {
        localeFilters += "en"
    }

    val proguardFiles = file("proguard").listFiles()!! +
            getDefaultProguardFile("proguard-android-optimize.txt")

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
        create("internal") {
            applicationIdSuffix = ".internal"
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            proguardFiles(
                *(proguardFiles + file("proguardInternal").listFiles()!!)
            )

            if (debugKeystoreExists) {
                signingConfig = signingConfigs.getByName("default")
            }

            matchingFallbacks += "release"
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true

            // Building a bundle with shrink resources is broken
            isShrinkResources = false
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
    packaging {
        resources {
            merges += "/META-INF/*"
            merges += "mozilla/public-suffix-list.txt"

            // Can happen if an archive was built incrementally and accidentally published as-is
            excludes += "**/previous-compilation-data.bin"

            // Kotlin coroutines test
            pickFirsts += "win32-x86-64/attach_hotspot_windows.dll"
            pickFirsts += "win32-x86/attach_hotspot_windows.dll"

            // Unknown
            pickFirsts += "META-INF/licenses/ASM"

            // Mockito inline
            pickFirsts += "mockito-extensions/org.mockito.plugins.MockMaker"
        }
    }

    lint {
        checkDependencies = true
    }
}

kotlin {
    compilerOptions {
        jvmToolchain(18)
        sourceSets.all {
            languageSettings {
                languageSettings.optIn("kotlin.RequiresOptIn")
            }
        }
        freeCompilerArgs.add("-Xcontext-receivers")
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    sourceSets {
        commonMain.dependencies {
            implementation(projects.modules.anime)
            implementation(projects.modules.anime2anime)
            implementation(projects.modules.anilist)
            implementation(projects.modules.art)
            implementation(projects.modules.browse)
            implementation(projects.modules.cds)
            implementation(projects.modules.image)
            implementation(projects.modules.data)
            implementation(projects.modules.entry)
            implementation(projects.modules.markdown)
            implementation(projects.modules.media)
            implementation(projects.modules.monetization)
            implementation(projects.modules.utilsInject)
            implementation(projects.modules.utilsRoom)
            implementation(projects.modules.settings)

            implementation(libs.kotlin.inject.runtime.kmp)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(libs.jetBrainsCompose.navigation.compose)
        }
        invokeWhenCreated("androidDebug") {
            dependencies {
                implementation(projects.modules.monetization.debug)
                implementation(projects.modules.animethemes)
                implementation(projects.modules.debug)
                implementation(libs.leakcanary.android)

                runtimeOnly(libs.cronet.embedded)
            }
        }
        invokeWhenCreated("androidInternal") {
            dependencies {
                implementation(projects.modules.debug)
                implementation(projects.modules.monetization.debug)
                implementation(projects.modules.animethemes)
                runtimeOnly(libs.cronet.embedded)
            }
        }
        invokeWhenCreated("androidRelease") {
            dependencies {
                implementation(projects.modules.play)
                implementation(projects.modules.monetization.unity)
                runtimeOnly(libs.cronet.play)
            }
        }
        androidMain.dependencies {
            runtimeOnly(libs.kotlin.reflect)

            implementation(libs.kotlinx.serialization.json)
            runtimeOnly(libs.kotlinx.coroutines.android)

            implementation(libs.paging.runtime.ktx)

            implementation(libs.androidx.core.ktx)
            implementation(libs.lifecycle.livedata.ktx)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.activity.compose)

            implementation(libs.androidx.security.crypto)

            runtimeOnly(libs.paging.runtime.ktx)

            runtimeOnly(libs.room.runtime)
            implementation(libs.room.paging)

            implementation(libs.work.runtime)
            implementation(libs.work.runtime.ktx)

            implementation(libs.commons.compress)
            implementation(libs.coil3.coil.compose)
            implementation(libs.coil3.coil.network.okhttp)
        }
    }
}

dependencies {
    add("kspAndroid", kspProcessors.room.compiler)
    add("kspAndroid", kspProcessors.kotlin.inject.compiler.ksp)
    add("kspCommonMainMetadata", kspProcessors.kotlin.inject.compiler.ksp)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

tasks.register("installAll") {
    dependsOn("installDebug", "installRelease", "installInternal")
}

fun Exec.launchActivity(
    packageName: String,
    activityName: String = "com.thekeeperofpie.artistalleydatabase.MainActivity",
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

tasks.register("launchInternal") {
    dependsOn("installInternal")
    finalizedBy("compileAndLaunchInternal")
    outputs.upToDateWhen { false }
}

tasks.register<Exec>("launchDebug") {
    dependsOn("installDebug")
    launchActivity("com.thekeeperofpie.anichive.debug")
    finalizedBy("installRelease", "installInternal")
    outputs.upToDateWhen { false }
}

tasks.register<Exec>("compileAndLaunchRelease") {
    commandLine(
        "adb", "shell", "pm", "compile", "-f",
        "-m", "everything",
        "--check-prof", "false",
        "com.thekeeperofpie.anichive",
    )
    finalizedBy("launchReleaseMainActivity")
    outputs.upToDateWhen { false }
}

tasks.register<Exec>("compileAndLaunchInternal") {
    commandLine(
        "adb", "shell", "pm", "compile", "-f",
        "-m", "everything",
        "--check-prof", "false",
        "com.thekeeperofpie.anichive.internal",
    )
    finalizedBy("launchInternalMainActivity")
    outputs.upToDateWhen { false }
}

tasks.register<Exec>("launchInternalMainActivity") {
    launchActivity("com.thekeeperofpie.anichive.internal")
    outputs.upToDateWhen { false }
}

tasks.register<Exec>("launchReleaseMainActivity") {
    launchActivity("com.thekeeperofpie.anichive")
    outputs.upToDateWhen { false }
}

tasks.getByPath("preBuild").dependsOn(":copyGitHooks")

tasks.named { it.contains("explodeCodeSource") }.configureEach {
    dependsOn("generateResourceAccessorsForAndroidMain")
    dependsOn("generateActualResourceCollectorsForAndroidMain")
}
