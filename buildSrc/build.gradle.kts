plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "4.0.7"
    id("org.gradle.kotlin.kotlin-dsl.precompiled-script-plugins") version "4.0.7"
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.2.0-alpha01")
    implementation("org.jetbrains.kotlin.android:org.jetbrains.kotlin.android.gradle.plugin:1.8.21")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.8.21-1.0.11")
    implementation("com.squareup:javapoet:1.13.0")
}
