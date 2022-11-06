@file:Suppress("UnstableApiUsage")

import android.annotation.SuppressLint

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.github.jk1.dependency-license-report") version "2.0"
    id("com.google.devtools.ksp") version "1.7.20-Beta-1.0.6"
    kotlin("plugin.serialization") version "1.7.20-Beta"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.thekeeperofpie.artistalleydatabase"
        minSdk = 31
        @SuppressLint("OldTargetApi")
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
            isCrunchPngs = false
            proguardFiles(*proguardFiles)

            if (debugKeystoreExists) {
                signingConfig = signingConfigs.getByName("default")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.0-dev-k1.7.20-Beta-18f49346e42"
    }
    packagingOptions {
        resources {
            merges += "/META-INF/{AL2.0,LGPL2.1,DEPENDENCIES}"
            merges += "mozilla/public-suffix-list.txt"
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
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

dependencies {
    implementation(project(":modules:android-utils"))
    implementation(project(":modules:anilist"))
    implementation(project(":modules:art"))
    implementation(project(":modules:browse"))
    implementation(project(":modules:cds"))
    implementation(project(":modules:compose-utils"))
    implementation(project(":modules:data"))
    implementation(project(":modules:form"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    implementation("androidx.navigation:navigation-compose:2.5.3")

    implementation("com.google.dagger:hilt-android:2.43.2")
    kapt("com.google.dagger:hilt-compiler:2.43.2")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.ui:ui:1.4.0-alpha01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0-alpha01")
    implementation("androidx.compose.material:material-icons-core:1.4.0-alpha01")
    implementation("androidx.compose.material:material-icons-extended:1.4.0-alpha01")
    implementation("androidx.compose.material3:material3:1.1.0-alpha01")

    implementation("androidx.paging:paging-runtime:3.2.0-alpha03")
    implementation("androidx.paging:paging-compose:1.0.0-alpha17")

    implementation("androidx.room:room-runtime:2.5.0-beta01")
    ksp("androidx.room:room-compiler:2.5.0-beta01")
    implementation("androidx.room:room-ktx:2.5.0-beta01")
    testImplementation("androidx.room:room-testing:2.5.0-beta01")
    implementation("androidx.room:room-paging:2.5.0-beta01")

    implementation("io.coil-kt:coil:2.2.0")
    implementation("io.coil-kt:coil-compose:2.2.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.0-alpha01")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0-alpha01")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.0-alpha01")

    implementation("com.mxalbert.sharedelements:shared-elements:0.1.0-SNAPSHOT")

    implementation("androidx.work:work-runtime:2.7.1")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    androidTestImplementation("androidx.work:work-testing:2.7.1")

    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")

    implementation("com.google.accompanist:accompanist-pager:0.26.1-alpha")

    // TODO: Re-add official pager-indicator library once it migrates to material3
    // implementation("com.google.accompanist:accompanist-pager-indicators:0.24.13-rc")
}