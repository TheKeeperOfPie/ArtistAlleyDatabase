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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven("https://jitpack.io/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")

        // https://youtrack.jetbrains.com/issue/KT-68533/Kotlin-2.0-WasmJs-error-when-using-RepositoriesMode.FAILONPROJECTREPOS#focus=Comments-27-10172670.0-0
        mavenCentral {
            content {
                excludeGroup("com.yarnpkg")
                excludeGroup("com.github.webassembly")
                excludeGroup("org.nodejs")
            }
        }
        google {
            content {
                excludeGroup("com.yarnpkg")
                excludeGroup("com.github.webassembly")
                excludeGroup("org.nodejs")
            }
        }
        exclusiveContent {
            forRepository {
                ivy("https://nodejs.org/dist/") {
                    name = "Node Distributions at $url"
                    patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("org.nodejs", "node") }
                }
            }
            filter { includeGroup("org.nodejs") }
        }
        exclusiveContent {
            forRepository {
                ivy("https://github.com/yarnpkg/yarn/releases/download") {
                    name = "Yarn Distributions at $url"
                    patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("com.yarnpkg", "yarn") }
                }
            }
            filter { includeGroup("com.yarnpkg") }
        }
        exclusiveContent {
            forRepository {
                ivy("https://github.com/WebAssembly/binaryen/releases/download") {
                    name = "Binaryen Distributions at $url"
                    patternLayout { artifact("version_[revision]/[module]-version_[revision]-[classifier].[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("com.github.webassembly", "binaryen") }
                }
            }
            filter { includeGroup("com.github.webassembly") }
        }

        flatDir { dirs = setOf(rootProject.projectDir.resolve("/libs")) }
    }
}

plugins {
    id("com.autonomousapps.build-health").version("2.16.0")
    id("com.android.application").version("8.13.2").apply(false)
    id("org.jetbrains.kotlin.android").version("2.3.20").apply(false)
    id("org.jetbrains.kotlin.jvm").version("2.3.20").apply(false)
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ArtistAlleyDatabase"
includeBuild("build-logic")
includeBuild("shared")
include(
    ":android-app",
    ":app",
    ":modules:alley",
    ":modules:alley:data",
    ":modules:alley:edit",
    ":modules:alley:form",
    ":modules:alley:models",
    ":modules:alley:user",
    ":modules:alley-app",
    ":modules:alley-app:service-worker",
    ":modules:alley-edit",
    ":modules:alley-form",
    ":modules:alley-functions",
    ":modules:alley-functions:middleware",
    ":modules:anilist",
    ":modules:anilist:data",
    ":modules:anime",
    ":modules:anime:activities",
    ":modules:anime:activities:data",
    ":modules:anime:characters",
    ":modules:anime:characters:data",
    ":modules:anime:data",
    ":modules:anime:favorites",
    ":modules:anime:forums",
    ":modules:anime:forums:data",
    ":modules:anime:history",
    ":modules:anime:ignore",
    ":modules:anime:ignore:data",
    ":modules:anime:ignore:testing",
    ":modules:anime:media:data",
    ":modules:anime:news",
    ":modules:anime:notifications",
    ":modules:anime:recommendations",
    ":modules:anime:reviews",
    ":modules:anime:schedule",
    ":modules:anime:search",
    ":modules:anime:search:data",
    ":modules:anime:seasonal",
    ":modules:anime:songs",
    ":modules:anime:staff",
    ":modules:anime:staff:data",
    ":modules:anime:studios",
    ":modules:anime:studios:data",
    ":modules:anime:ui",
    ":modules:anime:users",
    ":modules:anime:users:data",
    ":modules:anime2anime",
    ":modules:animethemes",
    ":modules:apollo",
    ":modules:apollo:utils",
    ":modules:art",
    ":modules:browse",
    ":modules:cds",
    ":modules:data",
    ":modules:debug",
    ":modules:entry",
    ":modules:image",
    ":modules:markdown",
    ":modules:media",
    ":modules:monetization",
    ":modules:monetization:debug",
    ":modules:monetization:unity",
    ":modules:musical-artists",
    ":modules:play",
    ":modules:secrets",
    ":modules:settings",
    ":modules:settings:ui",
    ":modules:test-utils",
    ":modules:utils",
    ":modules:utils-build-config",
    ":modules:utils-compose",
    ":modules:utils-inject",
    ":modules:utils-network",
    ":modules:utils-room",
    ":modules:vgmdb",
)
