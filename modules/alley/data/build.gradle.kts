
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Doesn't use convention plugins to avoid having to add a JS target to every module
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    compilerOptions {
        jvmToolchain(18)
    }

    androidLibrary {
        compileSdk = 36
        minSdk = 28

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget = JvmTarget.JVM_18
                }
            }
        }
    }
    jvm()
    js {
        browser()
        // https://youtrack.jetbrains.com/issue/KT-80175/K-JS-Task-with-name-jsBrowserProductionWebpack-not-found-in-project#focus=Comments-27-12543740.0-0
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        // https://youtrack.jetbrains.com/issue/KT-80175/K-JS-Task-with-name-jsBrowserProductionWebpack-not-found-in-project#focus=Comments-27-12543740.0-0
        binaries.executable()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(project.layout.buildDirectory.dir("generated/source"))
            dependencies {
                api("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.runtime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.uri.kmp)
            }
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.alley.data"
        compileSdk = 36
        minSdk = 28
    }
}

val inputsTask = tasks.register<ArtistAlleyProcessInputsTask>("processArtistAlleyInputs")
val databaseTask = tasks.register<ArtistAlleyDatabaseTask>("generateArtistAlleyDatabase") {
    inputImages.set(inputsTask.flatMap { it.outputResources })
    mustRunAfter(inputsTask)
}

compose.resources {
    publicResClass = true
    customDirectory(
        sourceSetName = "commonMain",
        // zip to force databaseTask to run
        directoryProvider = inputsTask.zip(databaseTask) { first, _ ->
            first.outputResources.get()
        },
    )
}
