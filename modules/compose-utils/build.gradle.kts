@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.thekeeperofpie.compose_proxy"
    compileSdk = 33

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
        kotlinCompilerExtensionVersion = "1.3.0-dev-k1.7.20-Beta-18f49346e42"
    }
}

dependencies {
    implementation("androidx.compose.material:material:1.3.0-beta01")
    implementation("androidx.compose.material3:material3:1.0.0-beta01")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.26.1-alpha")

    implementation("androidx.paging:paging-runtime:3.2.0-alpha02")
    implementation("androidx.paging:paging-compose:1.0.0-alpha16")
}