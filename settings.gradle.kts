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

rootProject.name = "ArtistAlleyDatabase"
include(
    ":app",
    ":modules:alley",
    ":modules:alley-app",
    ":modules:android-utils",
    ":modules:anilist",
    ":modules:anilist-data",
    ":modules:anime",
    ":modules:anime2anime",
    ":modules:animethemes",
    ":modules:apollo",
    ":modules:art",
    ":modules:browse",
    ":modules:compose-utils",
    ":modules:cds",
    ":modules:data",
    ":modules:debug",
    ":modules:dependencies",
    ":modules:entry",
    ":modules:markdown",
    ":modules:media",
    ":modules:monetization",
    ":modules:monetization:debug",
    ":modules:monetization:unity",
    ":modules:monetization:unity:secrets",
    ":modules:musical-artists",
    ":modules:play",
    ":modules:news",
    ":modules:server",
    ":modules:settings",
    ":modules:test-utils",
    ":modules:utils",
    ":modules:utils-compose",
    ":modules:utils-network",
    ":modules:utils-room",
    ":modules:vgmdb",
)

// Utilities

data class Prefix(val prefix: String, val builder: VersionCatalogBuilder)

// gav is group:artifact:version but shortened for readability
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun VersionCatalogBuilder.library(
    gav: String,
    alias: String? = null,
    prefix: String? = null,
) {
    val realPrefix = if (prefix == null) "" else "$prefix."
    val artifact = gav.substringAfter(":").substringBefore(":")
        .replaceFirstChar { it.lowercaseChar() }
    library(alias ?: (realPrefix + artifact), gav)
}

fun VersionCatalogBuilder.prefix(prefix: String, block: Prefix.() -> Unit) =
    Prefix(prefix, this).block()

fun Prefix.library(gav: String) = builder.library(gav = gav, prefix = prefix)
