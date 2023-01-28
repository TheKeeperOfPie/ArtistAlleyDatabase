plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    kotlin("plugin.serialization") version "1.8.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

kotlin {
    jvmToolchain(18)
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}