plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "4.0.6"
    id("org.gradle.kotlin.kotlin-dsl.precompiled-script-plugins") version "4.0.6"
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.1.0-alpha07")
    implementation("org.jetbrains.kotlin.android:org.jetbrains.kotlin.android.gradle.plugin:1.8.20-Beta")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.8.20-Beta-1.0.9")
    implementation("com.squareup:javapoet:1.13.0")
}