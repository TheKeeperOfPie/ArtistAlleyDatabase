plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-web")
    id("app.cash.sqldelight")
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(project.layout.buildDirectory.dir("generated/source"))
            dependencies {
                api("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
                api(projects.modules.alley.models)
                implementation(libs.jetBrainsCompose.components.resources)
                implementation(libs.jetBrainsCompose.ui.tooling.preview)
                implementation(libs.jetBrainsCompose.runtime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.uri.kmp)
            }
        }
    }
}

kotlin {
    android {
        namespace = "com.thekeeperofpie.artistalleydatabase.alley.data"
        compileSdk = 36
        minSdk = 28
    }
}

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.data")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.2.1")
            generateAsync = true
        }
    }
}

val inputsTask = tasks.register<ArtistAlleyProcessInputsTask>("processArtistAlleyInputs") {
    // Writing the cache is more expensive than running the task incrementally
    outputs.cacheIf { false }
}
val changelogTask = tasks.register<ArtistAlleyChangelogTask>("generateArtistAlleyChangelog")
val databaseTask = tasks.register<ArtistAlleyDatabaseTask>("generateArtistAlleyDatabase") {
    inputImagesAnimeExpo2023.set(inputsTask.flatMap { it.outputImagesAnimeExpo2023 })
    inputImagesAnimeExpo2024.set(inputsTask.flatMap { it.outputImagesAnimeExpo2024 })
    inputImagesAnimeExpo2025.set(inputsTask.flatMap { it.outputImagesAnimeExpo2025 })
    inputImagesAnimeNyc2025.set(inputsTask.flatMap { it.outputImagesAnimeNyc2025 })
    inputEmbeds.set(project.file("inputs/embeds"))
    inputChangelog.set(changelogTask.flatMap { it.outputFile })
    mustRunAfter(changelogTask, inputsTask)
}

compose.resources {
    publicResClass = true
    customDirectory(
        sourceSetName = "commonMain",
        // zip to force databaseTask to run
        directoryProvider = inputsTask.zip(databaseTask) { _, _ ->
            layout.buildDirectory.dir("generated/composeResources").get()
        },
    )
}
