@file:Suppress("UnstableApiUsage")

plugins {
    id("library-kotlin")
    id("library-js")
    id("library-wasmJs")
}

kotlin {
    sourceSets {
        webMain.dependencies {
            resolveLibraries("libs.kotlinx.browser").forEach(::implementation)
        }
    }
}
