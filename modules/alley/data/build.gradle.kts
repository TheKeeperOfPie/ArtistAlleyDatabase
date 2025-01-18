import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    // Doesn't use convention plugins to avoid having to add a JS target to every module
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("com.android.library")
}

kotlin {
    androidTarget()
    jvm()
    js { browser() }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain {
            kotlin.srcDir(project.layout.buildDirectory.dir("generated/source"))
            dependencies {
                implementation(compose.components.resources)
                implementation(compose.runtime)
                implementation(libs.uri.kmp)
            }
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.alley.data"
    compileSdk = 35
    defaultConfig {
        minSdk = 28
    }
}

tasks.register<ArtistAlleyDatabaseTask>("generateArtistAlleyDatabase")
val inputsTask = tasks.register<ArtistAlleyProcessInputsTask>("processArtistAlleyInputs") {
    dependsOn("generateArtistAlleyDatabase")
}

compose.resources {
    publicResClass = true
    customDirectory("commonMain", inputsTask.map { it.outputResources.get() })
}
