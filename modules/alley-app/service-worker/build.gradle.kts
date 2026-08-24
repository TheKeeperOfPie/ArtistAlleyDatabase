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
                mainOutputFileName = "serviceWorker.js"
            }
        }
    }

    sourceSets {
        jsMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
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
