@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.thekeeperofpie.compose_proxy"
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
    api("androidx.compose.material:material:1.4.0-alpha05")
    api("androidx.compose.material3:material3:1.1.0-alpha05")
    api("androidx.compose.ui:ui:1.4.0-alpha05")
    api("androidx.compose.ui:ui-tooling-preview:1.4.0-alpha05")
    implementation("androidx.activity:activity-compose:1.7.0-alpha04")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.29.0-alpha")

    api("androidx.paging:paging-compose:1.0.0-alpha17")
}