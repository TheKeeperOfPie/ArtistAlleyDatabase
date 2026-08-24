buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://central.sonatype.com/repository/maven-snapshots")
    }
    dependencies {
        classpath(libs.burst.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.androidx.room).apply(false)
    alias(libs.plugins.app.cash.burst).apply(false)
    alias(libs.plugins.app.cash.sqldelight).apply(false)
    alias(libs.plugins.com.google.devtools.ksp).apply(false)
    alias(libs.plugins.org.jetbrains.compose).apply(false)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.compose).apply(false)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization).apply(false)
    id(libs.plugins.com.android.application.get().pluginId).apply(false)
    id(libs.plugins.com.android.kotlin.multiplatform.library.get().pluginId).apply(false)
    id(libs.plugins.org.jetbrains.kotlin.android.get().pluginId).apply(false)
    id(libs.plugins.com.mikepenz.aboutlibraries.plugin.get().pluginId) version
            libs.plugins.com.mikepenz.aboutlibraries.plugin.get().version.toString() apply false
}

tasks.register("generateVerificationMetadata") {
    dependsOn("recopyVerificationMetadata")
    dependsOn("help")
    dependsOn("dependencyUpdates")
    finalizedBy(":app:licenseReleaseReport")
}
