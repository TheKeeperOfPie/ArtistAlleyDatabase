@file:Suppress("UnstableApiUsage")

plugins {
    id("library-kotlin")
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                sourceMaps = false
            }
            testTask {
                // CMP is wasmJs first, and regular js tests don't bundle Skiko correctly
                enabled = false
            }
        }
        binaries.executable()
    }
}
