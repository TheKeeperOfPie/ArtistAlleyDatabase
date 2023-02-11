@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "1.8.0-1.0.9"
    kotlin("plugin.serialization") version "1.8.0"
    id("de.mannodermaus.android-junit5")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.vgmdb"
    compileSdk = 33

    defaultConfig {
        minSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        testInstrumentationRunnerArguments["runnerBuilder"] =
            "de.mannodermaus.junit5.AndroidJUnit5Builder"
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
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
    packaging {
        resources {
            merges += "/META-INF/DEPENDENCIES"
        }
    }
}

kotlin {
    jvmToolchain(18)
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:entry"))

    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    implementation("com.google.dagger:hilt-android:2.44.2")
    kapt("com.google.dagger:hilt-compiler:2.44.2")

    runtimeOnly("androidx.room:room-runtime:2.5.0")
    ksp("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    testImplementation("androidx.room:room-testing:2.5.0")
    implementation("androidx.room:room-paging:2.5.0")

    implementation("it.skrape:skrapeit:1.3.0-alpha.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.mockito:mockito-core:5.0.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")

    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.3.0")
}

sourceSets {
    forEach {
        if (it !is KotlinSourceSet) return@forEach
        it.kotlin {
            srcDir(project.layout.buildDirectory.file("generated/source/jsonSchemaModels"))
            srcDir(project.layout.buildDirectory.file("generated/source/raml"))
        }
    }
}