@file:Suppress("UnstableApiUsage")

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
val Project.kspProcessors: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("kspProcessors")
plugins {
    id("library-kotlin")
    id("library-desktop")
    id("dev.zacsweers.metro")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":modules:utils-inject"))
        }
    }
}

metro {
    generateAssistedFactories.set(true)
}
