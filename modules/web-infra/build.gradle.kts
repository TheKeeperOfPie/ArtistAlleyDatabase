plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    kotlin("plugin.serialization") version "1.7.10"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}