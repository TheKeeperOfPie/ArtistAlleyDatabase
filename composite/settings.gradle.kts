@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        maven("https://repository.mulesoft.org/releases/") {
            content {
                includeGroup("com.github.amlorg")
                includeGroupByRegex("org\\.mule.*")
            }
        }
        maven("https://jitpack.io") {
            content {
                includeGroup("com.github.everit-org.json-schema")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "Artist Alley Database Composite Build"
include(
    ":json-schema",
    ":raml",
    ":utils",
)
