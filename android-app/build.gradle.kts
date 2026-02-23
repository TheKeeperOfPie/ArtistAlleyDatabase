@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("androidx.room")
    alias(libs.plugins.dev.zacsweers.metro)
    alias(libs.plugins.com.github.ben.manes.versions)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_18)
        }
    }
    compilerOptions  {
        freeCompilerArgs.add("-Xcontext-parameters")
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

android {
    namespace = "com.thekeeperofpie.anichive"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.thekeeperofpie.anichive"
        minSdk = 28 // TODO: App doesn't work on 28 emulator
        targetSdk = 34
        versionCode = 18
        versionName = "0.53"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    androidResources {
        localeFilters += "en"
    }

    val proguardFiles = arrayOf(
        file("proguard/proguard-rules.pro"),
        getDefaultProguardFile("proguard-android-optimize.txt")
    )

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
        }
    }

    lint {
        checkDependencies = true
    }
}

dependencies {
    implementation(projects.app)
    ksp(kspProcessors.room.compiler)

    // TODO: Figure out actual app dependencies, these were copied from :app when this module was
    //  split out
    implementation(compose.components.resources)
    implementation(compose.components.uiToolingPreview)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.runtime)
    implementation(compose.ui)
    implementation(libs.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.security.crypto)
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)
    implementation(libs.commons.compress)
    implementation(libs.jetBrainsAndroidX.lifecycle.viewmodel.compose)
    implementation(libs.jetBrainsAndroidX.navigation.compose)
    implementation(libs.kermit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room.paging)
    implementation(libs.work.runtime)
    implementation(libs.work.runtime.ktx)
    implementation(projects.modules.anilist)
    implementation(projects.modules.anime)
    implementation(projects.modules.anime2anime)
    implementation(projects.modules.art)
    implementation(projects.modules.browse)
    implementation(projects.modules.cds)
    implementation(projects.modules.data)
    implementation(projects.modules.entry)
    implementation(projects.modules.image)
    implementation(projects.modules.markdown)
    implementation(projects.modules.media)
    implementation(projects.modules.monetization)
    implementation(projects.modules.settings)
    implementation(projects.modules.utils)
    implementation(projects.modules.utilsBuildConfig)
    implementation(projects.modules.utilsInject)
    implementation(projects.modules.utilsNetwork)
    implementation(projects.modules.utilsRoom)
    runtimeOnly(libs.kotlin.reflect)
    runtimeOnly(libs.kotlinx.coroutines.android)
    runtimeOnly(libs.room.runtime)

    debugImplementation(projects.modules.monetization.debug)
    debugImplementation(projects.modules.animethemes)
    debugImplementation(projects.modules.debug)
    debugImplementation(libs.leakcanary.android)
    // TODO: Restore Cronet support
//    debugRuntimeOnly(libs.cronet.embedded)

    "internalImplementation"(projects.modules.debug)
    "internalImplementation"(projects.modules.monetization.debug)
    "internalImplementation"(projects.modules.animethemes)
//    "internalRuntimeOnly"(libs.cronet.embedded)

    releaseImplementation(projects.modules.play)
    releaseImplementation(projects.modules.monetization.unity)
//    releaseRuntimeOnly(libs.cronet.play)
}

room {
    schemaDirectory("$projectDir/schemas")
    generateKotlin = true
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
