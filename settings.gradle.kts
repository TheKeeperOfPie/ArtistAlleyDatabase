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

apply(rootProject.projectDir.resolve("versions.gradle.kts"))
@Suppress("UNCHECKED_CAST")
(extra["versions"] as (DependencyResolutionManagement) -> Unit)(dependencyResolutionManagement)

plugins {
    id("com.autonomousapps.build-health").version("2.16.0")
    id("com.android.application").version("8.13.1").apply(false)
    id("org.jetbrains.kotlin.android").version("2.3.0-RC2").apply(false)
    id("org.jetbrains.kotlin.jvm").version("2.3.0-RC2").apply(false)
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ArtistAlleyDatabase"
includeBuild("build-logic")
includeBuild("shared")
include(":app")

val excludedDirectories = setOf(".idea", "build")
file("modules")
    .walkTopDown()
    .onEnter { !excludedDirectories.contains(it.name) }
    .filter { it.isDirectory }
    .filter { it.list()?.contains("build.gradle.kts") == true }
    .map { it.relativeTo(rootProject.projectDir).path.replace(File.separator, ":") }
    .toList()
    .let { include(it) }
