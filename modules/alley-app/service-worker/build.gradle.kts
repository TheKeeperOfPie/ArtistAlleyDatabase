import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    js(compiler = KotlinJsCompilerType.IR) {
        binaries.executable()
        browser {
            webpackTask {
                mainOutputFileName = "serviceWorker.js"
            }
        }
    }

    compilerOptions {
        jvmToolchain(18)
        freeCompilerArgs.add("-Xcontext-receivers")
    }

    sourceSets {
        jsMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(projects.modules.alley.data)
        }
    }
}

val distribution: NamedDomainObjectProvider<Configuration> by configurations.registering {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(distribution.name, tasks.named("jsBrowserDistribution"))
}
