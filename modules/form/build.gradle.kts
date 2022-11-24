@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.form"
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
    api(project(":modules:compose-utils"))
    api("io.github.hoc081098:FlowExt:0.4.0")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.core:core-ktx:1.9.0")
    api("androidx.compose.ui:ui:1.4.0-alpha02")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0-alpha02")
    implementation("androidx.compose.material:material-icons-core:1.4.0-alpha02")
    implementation("androidx.compose.material:material-icons-extended:1.4.0-alpha02")
    implementation("androidx.compose.material3:material3:1.1.0-alpha02")

    implementation("androidx.navigation:navigation-compose:2.5.3")

    implementation("androidx.paging:paging-compose:1.0.0-alpha17")

    implementation("io.coil-kt:coil-compose:2.2.0")

    implementation("com.mxalbert.sharedelements:shared-elements:0.1.0-SNAPSHOT")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
}