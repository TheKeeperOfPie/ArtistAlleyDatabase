@file:Suppress("UnstableApiUsage")

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
plugins {
    id("library-kotlin")
    id("library-js")
    id("library-wasmJs")
}

kotlin {
    sourceSets {
        webMain.dependencies {
            libs.find(
                "libs.kotlinx.browser",
            ).forEach(::implementation)
        }
    }
}
