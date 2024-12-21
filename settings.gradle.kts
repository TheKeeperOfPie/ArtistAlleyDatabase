@file:Suppress("UnstableApiUsage")

buildCache {
    local {
        directory = File(rootDir, "build-cache")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

apply(rootProject.projectDir.resolve("versions.gradle.kts"))
dependencyResolutionManagement {
    @Suppress("UNCHECKED_CAST")
    (extra["versions"] as (DependencyResolutionManagement) -> Unit)(this)
}

plugins {
    id("com.autonomousapps.build-health").version("2.5.0")
    id("com.android.application").version("8.9.0-alpha06").apply(false)
    id("org.jetbrains.kotlin.android").version("2.1.20-Beta1").apply(false)
    id("org.jetbrains.kotlin.jvm").version("2.1.20-Beta1").apply(false)
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ArtistAlleyDatabase"
includeBuild("build-logic")
include(":app")
include(":desktop")

val excludedDirectories = setOf(".idea", "build", "alley-app", "alley")
file("modules")
    .walkTopDown()
    .onEnter { !excludedDirectories.contains(it.name) }
    .filter { it.isDirectory }
    .filter { it.list()?.contains("build.gradle.kts") == true }
    .map { it.relativeTo(rootProject.projectDir).path.replace(File.separator, ":") }
    .toList()
    .let { include(it) }
