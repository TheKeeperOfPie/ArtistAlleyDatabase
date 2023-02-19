@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    kotlin("android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase"
    compileSdkPreview = "UpsideDownCake"

    defaultConfig {
        applicationId = "com.thekeeperofpie.artistalleydatabase"
        minSdk = 33
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
        kotlinCompilerExtensionVersion = "1.4.2"
    }
    packaging {
        resources {
            merges += "/META-INF/{AL2.0,LGPL2.1,DEPENDENCIES}"
            merges += "mozilla/public-suffix-list.txt"
        }
    }
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
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
    implementation(project(":modules:anilist"))
    implementation(project(":modules:art"))
    implementation(project(":modules:browse"))
    implementation(project(":modules:cds"))
    implementation(project(":modules:compose-utils"))
    implementation(project(":modules:data"))
    implementation(project(":modules:entry"))
    runtimeOnly(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    implementation("androidx.navigation:navigation-compose:2.6.0-alpha05")

    implementation("com.google.dagger:hilt-android:2.45")
    kapt("com.google.dagger:hilt-compiler:2.45")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0-alpha01")
    implementation("androidx.hilt:hilt-work:1.0.0")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.0-beta01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0-beta01")
    implementation("androidx.activity:activity-compose:1.8.0-alpha01")
    implementation("androidx.compose.ui:ui:1.4.0-beta01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0-beta01")
    implementation("androidx.compose.material:material-icons-core:1.4.0-beta01")
    implementation("androidx.compose.material:material-icons-extended:1.4.0-beta01")
    implementation("androidx.compose.material3:material3:1.1.0-alpha06")

    runtimeOnly("androidx.paging:paging-runtime:3.2.0-alpha04")
    implementation("androidx.paging:paging-compose:1.0.0-alpha18")

    runtimeOnly("androidx.room:room-runtime:2.5.0")
    ksp("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    testImplementation("androidx.room:room-testing:2.5.0")
    implementation("androidx.room:room-paging:2.5.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    debugRuntimeOnly("androidx.compose.ui:ui-tooling:1.4.0-beta01")
    debugRuntimeOnly("androidx.compose.ui:ui-test-manifest:1.4.0-beta01")

//    implementation("com.mxalbert.sharedelements:shared-elements:0.1.0-SNAPSHOT")
    implementation(group = "", name = "shared-elements-0.1.0-20221204.093513-11", ext = "aar")

    implementation("androidx.work:work-runtime:2.8.0")
    implementation("androidx.work:work-runtime-ktx:2.8.0")

    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
}