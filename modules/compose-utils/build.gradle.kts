@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.thekeeperofpie.compose_proxy"
    compileSdk = 32

    defaultConfig {
        minSdk = 31

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
        kotlinCompilerExtensionVersion = "1.2.0-dev-k1.7.0-53370d83bb1"
    }
}

dependencies {
    implementation("androidx.compose.material:material:1.3.0-alpha03")
    implementation("androidx.compose.material3:material3:1.0.0-alpha16")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.24.13-rc")

    implementation("androidx.paging:paging-runtime:3.2.0-alpha02")
    implementation("androidx.paging:paging-compose:1.0.0-alpha16")
}