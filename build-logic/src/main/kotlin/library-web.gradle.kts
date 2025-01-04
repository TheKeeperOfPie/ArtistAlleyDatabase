@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl


plugins {
    id("library-kotlin")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }
}
