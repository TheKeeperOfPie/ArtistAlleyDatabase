@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "1.7.20-Beta-1.0.6"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.browse"
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
    implementation(project(":modules:android-utils"))
    implementation(project(":modules:compose-utils"))
    implementation(project(":modules:form"))

    implementation("androidx.navigation:navigation-compose:2.5.2")

    implementation("com.google.dagger:hilt-android:2.43.2")
    kapt("com.google.dagger:hilt-compiler:2.43.2")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("androidx.activity:activity-compose:1.6.0")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.compose.ui:ui:1.3.0-beta03")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.0-beta03")
    api("androidx.compose.material:material-icons-core:1.3.0-beta03")
    api("androidx.compose.material:material-icons-extended:1.3.0-beta03")
    implementation("androidx.compose.material3:material3:1.0.0-beta02")

    implementation("androidx.paging:paging-runtime:3.2.0-alpha02")
    implementation("androidx.paging:paging-compose:1.0.0-alpha16")

    implementation("io.coil-kt:coil:2.2.0")
    implementation("io.coil-kt:coil-compose:2.2.0")

    implementation("com.mxalbert.sharedelements:shared-elements:0.1.0-SNAPSHOT")

    implementation("com.google.accompanist:accompanist-pager:0.26.1-alpha")

    // TODO: Re-add official pager-indicator library once it migrates to material3
    // implementation("com.google.accompanist:accompanist-pager-indicators:0.24.13-rc")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}