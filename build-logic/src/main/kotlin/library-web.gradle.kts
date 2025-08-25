@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl


plugins {
    id("library-kotlin")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        // https://youtrack.jetbrains.com/issue/KT-80175/K-JS-Task-with-name-jsBrowserProductionWebpack-not-found-in-project#focus=Comments-27-12543740.0-0
        binaries.executable()
    }
    js {
        browser()
        // https://youtrack.jetbrains.com/issue/KT-80175/K-JS-Task-with-name-jsBrowserProductionWebpack-not-found-in-project#focus=Comments-27-12543740.0-0
        binaries.executable()
    }
}
