@file:Suppress("UnstableApiUsage")

plugins {
    id("library-kotlin")
}

kotlin {
    wasmJs { browser() }
}
