@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("com.android.lint")
}

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_18
        jvmToolchain(18)
        sourceSets.all {
            languageSettings {
                languageSettings.optIn("kotlin.RequiresOptIn")
            }
        }
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
