@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    includeBuild("composite")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        maven("https://androidx.dev/storage/compose-compiler/repository/")
        google()
        mavenCentral()
    }
}
rootProject.name = "Artist Alley Database"
include(
    ":app",
    ":modules:anilist",
    ":modules:art",
    ":modules:compose-utils",
    ":modules:cds",
    ":modules:form",
    ":modules:utils",
    ":modules:vgmdb",
    ":modules:web-infra",
)
