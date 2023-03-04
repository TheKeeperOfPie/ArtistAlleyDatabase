@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    id("module-library")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
    id("de.mannodermaus.android-junit5")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.vgmdb"
}

dependencies {
    api(project(":modules:android-utils"))
    api(project(":modules:entry"))

    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("com.google.dagger:hilt-android:2.45")
    kapt("com.google.dagger:hilt-compiler:2.45")

    runtimeOnly("androidx.room:room-runtime:2.5.0")
    ksp("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    testImplementation("androidx.room:room-testing:2.5.0")
    implementation("androidx.room:room-paging:2.5.0")

    implementation("it.skrape:skrapeit:1.3.0-alpha.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.mockito:mockito-core:5.1.1")
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