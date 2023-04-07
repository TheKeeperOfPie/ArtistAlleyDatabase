plugins {
    id("com.android.application")
    kotlin("android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.com.jaredsburrows.license)
}

licenseReport {
    // TODO: Not yet supported in latest stable plugin version
    copyHtmlReportToAssets = false
//    useVariantSpecificAssetDirs = false
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase"
    compileSdkPreview = "UpsideDownCake"

    defaultConfig {
        applicationId = "com.thekeeperofpie.artistalleydatabase"
        minSdk = 29
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
            isMinifyEnabled = false
            isShrinkResources = false
            isCrunchPngs = true
            proguardFiles(*proguardFiles)

            if (debugKeystoreExists) {
                signingConfig = signingConfigs.getByName("default")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.4-dev-k1.8.20-f6ae19e64ff"
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

kotlin {
    jvmToolchain(18)
    sourceSets.all {
        languageSettings {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

tasks.register("installAll") {
    dependsOn("installDebug", "installRelease")
}

fun Exec.launchActivity(
    packageName: String,
    activityName: String = "com.thekeeperofpie.artistalleydatabase.MainActivity"
) {
    commandLine(
        "adb", "shell", "am", "start-activity",
        "-a", "\"android.intent.action.MAIN\"",
        "-c", "\"android.intent.category.LAUNCHER\"",
        "-n", "\"$packageName/$activityName\"",
    )
}

tasks.register<Exec>("launchRelease") {
    dependsOn("installRelease")
    launchActivity("com.thekeeperofpie.artistalleydatabase")
    finalizedBy("installDebug")
    outputs.upToDateWhen { false }
}

tasks.register<Exec>("launchDebug") {
    dependsOn("installDebug")
    launchActivity("com.thekeeperofpie.artistalleydatabase.debug")
    finalizedBy("installRelease")
    outputs.upToDateWhen { false }
}

tasks.getByPath("preBuild").dependsOn(":copyGitHooks")

tasks.register("debugTask") {
    println("repositories = ${rootProject.repositories.size}")
    rootProject.repositories.forEach {
        println("Repo = ${it.name}, $it")
    }
}

dependencies {
    implementation(project(":modules:android-utils"))
    implementation(project(":modules:anime"))
    implementation(project(":modules:anilist"))
    implementation(project(":modules:art"))
    implementation(project(":modules:browse"))
    implementation(project(":modules:cds"))
    implementation(project(":modules:compose-utils"))
    implementation(project(":modules:data"))
    implementation(project(":modules:entry"))
    implementation(project(":modules:settings"))

    runtimeOnly(kotlin("reflect"))

    implementation(libs.kotlinx.serialization.json)
    runtimeOnly(libs.kotlinx.coroutines.android)

    implementation(libs.navigation.compose)

    implementation(libs.hilt.android)
    kapt(kaptProcessors.dagger.hilt.compiler)
    kapt(kaptProcessors.androidx.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material.icons.core)
    implementation(libs.material.icons.extended)
    implementation(libs.material3)
    implementation(libs.androidx.security.crypto)

    runtimeOnly(libs.paging.runtime)
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

//    implementation("com.mxalbert.sharedelements:shared-elements:0.1.0-SNAPSHOT")
    implementation(group = "", name = "shared-elements-0.1.0-20221204.093513-11", ext = "aar")

    implementation(libs.work.runtime)
    implementation(libs.work.runtime.ktx)

    implementation(libs.moshi.kotlin)
    ksp(kspProcessors.moshi.kotlin.codegen)

    implementation(libs.commons.compress)
}
