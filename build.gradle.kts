buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.42")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.8.2.1")
    }
}

plugins {
    id("com.android.application") version "7.4.0-alpha10" apply false
    id("com.android.library") version "7.4.0-alpha10" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20-Beta" apply false
    id("org.jetbrains.kotlin.jvm") version "1.7.20-Beta" apply false
}