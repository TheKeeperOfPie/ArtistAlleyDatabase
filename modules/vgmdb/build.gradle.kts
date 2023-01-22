@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "1.7.20-Beta-1.0.6"
    kotlin("plugin.serialization") version "1.7.20-Beta"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packagingOptions {
        resources {
            merges += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:form"))

    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("com.google.dagger:hilt-android:2.43.2")
    kapt("com.google.dagger:hilt-compiler:2.43.2")

    runtimeOnly("androidx.room:room-runtime:2.5.0-beta02")
    ksp("androidx.room:room-compiler:2.5.0-beta02")
    implementation("androidx.room:room-ktx:2.5.0-beta02")
    testImplementation("androidx.room:room-testing:2.5.0-beta02")
    implementation("androidx.room:room-paging:2.5.0-beta02")

    implementation("it.skrape:skrapeit:1.2.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.mockito:mockito-core:4.9.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.3.0")
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