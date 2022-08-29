@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "1.7.20-Beta-1.0.6"
    kotlin("plugin.serialization") version "1.7.20-Beta"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.cds"
    compileSdk = 33

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    implementation(project(":modules:anilist"))
    implementation(project(":modules:compose-utils"))
    implementation(project(":modules:form"))
    implementation(project(":modules:vgmdb"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    implementation("com.google.dagger:hilt-android:2.43.2")
    kapt("com.google.dagger:hilt-compiler:2.43.2")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("androidx.navigation:navigation-compose:2.5.1")

    implementation("androidx.activity:activity-compose:1.5.1")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.compose.ui:ui:1.3.0-beta01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.0-beta01")
    implementation("androidx.compose.material:material-icons-core:1.3.0-beta01")
    implementation("androidx.compose.material:material-icons-extended:1.3.0-beta01")
    implementation("androidx.compose.material3:material3:1.0.0-beta01")

    implementation("androidx.paging:paging-runtime:3.2.0-alpha02")
    implementation("androidx.paging:paging-compose:1.0.0-alpha16")

    implementation("androidx.room:room-runtime:2.5.0-alpha03")
    ksp("androidx.room:room-compiler:2.5.0-alpha03")
    implementation("androidx.room:room-ktx:2.5.0-alpha03")
    testImplementation("androidx.room:room-testing:2.5.0-alpha03")
    implementation("androidx.room:room-paging:2.5.0-alpha03")

    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")

    implementation("io.coil-kt:coil:2.2.0")
    implementation("io.coil-kt:coil-compose:2.2.0")

    implementation("com.mxalbert.sharedelements:shared-elements:0.1.0-SNAPSHOT")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}