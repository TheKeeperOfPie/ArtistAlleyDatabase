@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.8.0-1.0.9"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.android_utils"
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
}

kotlin {
    jvmToolchain(18)
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")

    runtimeOnly("androidx.work:work-runtime:2.8.0-rc01")
    api("androidx.work:work-runtime-ktx:2.8.0-rc01")
    api("io.github.hoc081098:FlowExt:0.5.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    runtimeOnly("androidx.room:room-runtime:2.5.0")
    ksp("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    testImplementation("androidx.room:room-testing:2.5.0")
    implementation("androidx.room:room-paging:2.5.0")

    api("com.squareup.moshi:moshi-kotlin:1.14.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}