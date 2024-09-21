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
    id("com.autonomousapps.build-health").version("2.0.2")
    id("com.android.application").version("8.8.0-alpha02").apply(false)
    id("org.jetbrains.kotlin.android").version("2.1.0-Beta1").apply(false)
    id("org.jetbrains.kotlin.jvm").version("2.1.0-Beta1").apply(false)
}

rootProject.name = "ArtistAlleyDatabase"
includeBuild("build-logic")
include(
    ":app",
    ":desktop",
//    ":modules:alley",
//    ":modules:alley-app",
    ":modules:anilist",
    ":modules:anilist-data",
    ":modules:anime",
    ":modules:anime:data",
    ":modules:anime2anime",
    ":modules:animethemes",
    ":modules:apollo",
    ":modules:apollo:utils",
    ":modules:art",
    ":modules:browse",
    ":modules:cds",
    ":modules:data",
    ":modules:debug",
    ":modules:dependencies",
    ":modules:entry",
    ":modules:image",
    ":modules:markdown",
    ":modules:media",
    ":modules:monetization",
    ":modules:monetization:debug",
    ":modules:monetization:unity",
    ":modules:musical-artists",
    ":modules:play",
    ":modules:news",
    ":modules:secrets",
    ":modules:server",
    ":modules:settings",
    ":modules:test-utils",
    ":modules:utils",
    ":modules:utils-compose",
    ":modules:utils-inject",
    ":modules:utils-network",
    ":modules:utils-room",
    ":modules:vgmdb",
)
