import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    kotlin("android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.com.jaredsburrows.license)
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("android-test-library")
    id("org.jetbrains.kotlin.plugin.compose")
}

secrets {
    propertiesFileName = "secrets.properties"
}

licenseReport {
    // TODO: Not yet supported in latest stable plugin version
    copyHtmlReportToAssets = false
//    useVariantSpecificAssetDirs = false
}

android {
    namespace = "com.thekeeperofpie.anichive"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thekeeperofpie.anichive"
        minSdk = 28
        targetSdk = 33
        versionCode = 12
        versionName = "0.47"

        testInstrumentationRunner = "com.thekeeperofpie.artistalleydatabase.test_utils.CustomAndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resourceConfigurations += "en"
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
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
        jvmTarget = JvmTarget.JVM_18
        jvmToolchain(18)
        sourceSets.all {
            languageSettings {
                languageSettings.optIn("kotlin.RequiresOptIn")
            }
        }
        freeCompilerArgs.add("-Xcontext-receivers")
    }
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

dependencies {
    implementation(project(":modules:android-utils"))
    implementation(project(":modules:anime"))
    implementation(project(":modules:anime2anime"))
    implementation(project(":modules:anilist"))
    implementation(project(":modules:art"))
    implementation(project(":modules:browse"))
    implementation(project(":modules:cds"))
    implementation(project(":modules:compose-utils"))
    implementation(project(":modules:data"))
    implementation(project(":modules:entry"))
    implementation(project(":modules:monetization"))
//    debugImplementation(project(":modules:monetization:debug"))

    debugImplementation(project(":modules:animethemes"))
    debugImplementation(project(":modules:debug"))
    "internalImplementation"(project(":modules:debug"))
    "internalImplementation"(project(":modules:monetization:debug"))
    "internalImplementation"(project(":modules:animethemes"))

    releaseImplementation(project(":modules:play"))
    releaseImplementation(project(":modules:monetization:unity"))
    implementation(project(":modules:settings"))

    runtimeOnly(kotlin("reflect"))

    implementation(libs.kotlinx.serialization.json)
    runtimeOnly(libs.kotlinx.coroutines.android)

    implementation(libs.navigation.compose)
    implementation(libs.paging.compose)

    implementation(libs.hilt.android)
    ksp(kspProcessors.hilt.compiler)
    ksp(kspProcessors.androidx.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    runtimeOnly(libs.compose.runtime.tracing)

    implementation(libs.material3)
    implementation(libs.androidx.security.crypto)

    runtimeOnly(libs.paging.runtime.ktx)
    runtimeOnly(libs.paging.compose)

    runtimeOnly(libs.room.runtime)
    ksp(kspProcessors.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.test)
    debugRuntimeOnly(libs.compose.ui.tooling)
    debugRuntimeOnly(libs.compose.ui.test.manifest)

    implementation(libs.work.runtime)
    implementation(libs.work.runtime.ktx)

    implementation(libs.moshi.kotlin)
    ksp(kspProcessors.moshi.kotlin.codegen)

    implementation(libs.commons.compress)
    implementation(libs.coil3.coil.network.okhttp)

    implementation(libs.play.services.oss.licenses)

    debugImplementation(libs.leakcanary.android)
}
