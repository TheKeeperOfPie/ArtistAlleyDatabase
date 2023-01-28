@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "1.8.0-1.0.9"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.browse"
    compileSdk = 33

    defaultConfig {
        minSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0"
    }
}

kotlin {
    jvmToolchain(18)
}

dependencies {
    api(project(":modules:android-utils"))
    implementation(project(":modules:compose-utils"))
    implementation(project(":modules:form"))

    api("androidx.navigation:navigation-compose:2.6.0-alpha04")

    implementation("com.google.dagger:hilt-android:2.44.2")
    kapt("com.google.dagger:hilt-compiler:2.44.2")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("androidx.compose.ui:ui:1.4.0-alpha05")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0-alpha05")
    implementation("androidx.compose.material3:material3:1.1.0-alpha05")

    implementation("com.google.accompanist:accompanist-pager:0.29.0-alpha")
    // TODO: Re-add official pager-indicator library once it migrates to material3
    // implementation("com.google.accompanist:accompanist-pager-indicators:0.24.13-rc")
}