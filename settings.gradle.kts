@file:Suppress("UnstableApiUsage")

buildCache {
    local {
        directory = File(rootDir, "build-cache")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            // https://github.com/sqldelight/sqldelight/pull/5534#issuecomment-2507584197
            url = uri("https://maven.pkg.github.com/edna-aa/sqldelight")
            credentials {
                // Borrowed from https://github.com/0ffz/gpr-for-gradle
                username = "token"
                password =
                    "\u0037\u0066\u0066\u0036\u0030\u0039\u0033\u0066\u0032\u0037\u0033\u0036\u0033\u0037\u0064\u0036\u0037\u0066\u0038\u0030\u0034\u0039\u0062\u0030\u0039\u0038\u0039\u0038\u0066\u0034\u0066\u0034\u0031\u0064\u0062\u0033\u0064\u0033\u0038\u0065"
            }
            content {
                includeGroup("app.cash.sqldelight")
                includeVersionByRegex("app.cash.sqldelight", ".*", ".*-wasm.*")
            }
        }
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenLocal {
            content {
                includeGroup("io.coil-kt.coil3")
            }
        }
        maven("https://jitpack.io/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev/")

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

        // https://github.com/cashapp/multiplatform-paging/pull/376
        maven {
            url = uri("https://maven.pkg.github.com/edna-aa/sqldelight")
            credentials {
                // Borrowed from https://github.com/0ffz/gpr-for-gradle
                username = "token"
                password =
                    "\u0037\u0066\u0066\u0036\u0030\u0039\u0033\u0066\u0032\u0037\u0033\u0036\u0033\u0037\u0064\u0036\u0037\u0066\u0038\u0030\u0034\u0039\u0062\u0030\u0039\u0038\u0039\u0038\u0066\u0034\u0066\u0034\u0031\u0064\u0062\u0033\u0064\u0033\u0038\u0065"
            }
            content {
                includeGroup("app.cash.sqldelight")
                includeGroup("app.cash.paging")
                includeVersionByRegex("app.cash.sqldelight", ".*", ".*-wasm.*")
                includeVersionByRegex("app.cash.paging", ".*", ".*-wasm.*")
            }
        }

        flatDir { dirs = setOf(rootProject.projectDir.resolve("/libs")) }
    }
}

apply(rootProject.projectDir.resolve("versions.gradle.kts"))
@Suppress("UNCHECKED_CAST")
(extra["versions"] as (DependencyResolutionManagement) -> Unit)(dependencyResolutionManagement)

plugins {
    id("com.autonomousapps.build-health").version("2.5.0")
    id("com.android.application").version("8.11.0-alpha04").apply(false)
    id("org.jetbrains.kotlin.android").version("2.1.20").apply(false)
    id("org.jetbrains.kotlin.jvm").version("2.1.20").apply(false)
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ArtistAlleyDatabase"
includeBuild("build-logic")
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
