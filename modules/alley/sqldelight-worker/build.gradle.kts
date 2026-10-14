plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                sourceMaps = false
            }
            webpackTask {
                mainOutputFileName = "sqlDelightWorker.js"
            }
        }
    }

    sourceSets {
        jsMain.dependencies {
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.coroutines.core)
            implementation(npm("@sqlite.org/sqlite-wasm", "^3.47.2-build1"))
        }
    }
}

val distribution = configurations.create("distribution") {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(distribution.name, tasks.named("jsBrowserDistribution"))
}
