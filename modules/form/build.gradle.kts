@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.form"
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
    implementation(project(":modules:android-utils"))
    api(project(":modules:compose-utils"))
    api("io.github.hoc081098:FlowExt:0.5.0")
    implementation("androidx.activity:activity-compose:1.7.0-alpha04")
    implementation("androidx.core:core-ktx:1.9.0")
    api("androidx.compose.ui:ui:1.4.0-alpha05")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0-alpha05")
    implementation("androidx.compose.material:material-icons-core:1.4.0-alpha05")
    implementation("androidx.compose.material:material-icons-extended:1.4.0-alpha05")
    implementation("androidx.compose.material3:material3:1.1.0-alpha05")

    implementation("androidx.navigation:navigation-compose:2.6.0-alpha04")

    implementation("androidx.paging:paging-compose:1.0.0-alpha17")

    implementation("io.coil-kt:coil-compose:2.2.1")

//    implementation("com.mxalbert.sharedelements:shared-elements:0.1.0-SNAPSHOT")
    implementation(group = "", name = "shared-elements-0.1.0-20221204.093513-11", ext = "aar")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}