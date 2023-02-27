@file:Suppress("UnstableApiUsage")

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
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        maven("https://androidx.dev/storage/compose-compiler/repository/")
        google()
        mavenCentral()
        flatDir { dirs = setOf(rootProject.projectDir.resolve("libs")) }
    }
}
rootProject.name = "Artist Alley Database"
include(
    ":app",
    ":modules:android-utils",
    ":modules:anilist",
    ":modules:art",
    ":modules:browse",
    ":modules:compose-utils",
    ":modules:cds",
    ":modules:data",
    ":modules:dependencies",
    ":modules:entry",
    ":modules:musical-artists",
    ":modules:settings",
    ":modules:vgmdb",
    ":modules:web-infra",
)
