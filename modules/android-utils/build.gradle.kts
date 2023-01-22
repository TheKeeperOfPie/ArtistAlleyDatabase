@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.7.20-Beta-1.0.6"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    runtimeOnly("androidx.work:work-runtime:2.7.1")
    api("androidx.work:work-runtime-ktx:2.7.1")
    api("io.github.hoc081098:FlowExt:0.4.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    runtimeOnly("androidx.room:room-runtime:2.5.0-beta02")
    ksp("androidx.room:room-compiler:2.5.0-beta02")
    implementation("androidx.room:room-ktx:2.5.0-beta02")
    testImplementation("androidx.room:room-testing:2.5.0-beta02")
    implementation("androidx.room:room-paging:2.5.0-beta02")

    api("com.squareup.moshi:moshi-kotlin:1.13.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
}