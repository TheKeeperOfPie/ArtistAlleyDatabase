pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}


apply(file("../versions.gradle.kts"))
dependencyResolutionManagement {
    @Suppress("UNCHECKED_CAST")
    (extra["versions"] as (DependencyResolutionManagement) -> Unit)(this)
}

rootProject.name = "shared"
