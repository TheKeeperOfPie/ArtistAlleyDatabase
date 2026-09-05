
import org.jetbrains.compose.resources.ResourcesExtension.ResourceClassGeneration

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
        compileSdk = 37
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
val changelogTask = tasks.register<ArtistAlleyChangelogTask>("generateArtistAlleyChangelog") {
    legacySeriesFile.set(project.file("inputs/tags/seriesLegacy.sql"))
    legacyMerchFile.set(project.file("inputs/tags/merchLegacy.sql"))
}
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
    generateResClass = ResourceClassGeneration.Always
}

val syncAlleyResources = tasks.register<Sync>("syncAlleyResources") {
    dependsOn(inputsTask, databaseTask)
    from(databaseTask.flatMap { it.outputDatabaseFile })
    from(databaseTask.flatMap { it.outputDatabaseHashFile })
    from(databaseTask.flatMap { it.outputEmbedImages }) {
        into("embeds")
    }
    from(inputsTask.flatMap { it.outputImagesAnimeExpo2023 }) {
        into("images/2023")
    }
    from(inputsTask.flatMap { it.outputImagesAnimeExpo2024 }) {
        into("images/2024")
    }
    from(inputsTask.flatMap { it.outputImagesAnimeExpo2025 }) {
        into("images/2025")
    }
    from(inputsTask.flatMap { it.outputImagesAnimeNyc2025 }) {
        into("images/animeNyc2025")
    }
    from(databaseTask.flatMap { it.outputImagesAnimeExpo2026 }) {
        into("images/AX2026")
    }
    from(databaseTask.flatMap { it.outputImagesAnimeNyc2026 }) {
        into("images/ANYC2026")
    }
    into(layout.buildDirectory.dir("alleyResources/composeResources/artistalleydatabase.modules.alley.data.generated.resources/files"))
}

tasks.withType<Jar>().configureEach {
    from(syncAlleyResources.map { it.destinationDir.parentFile.parentFile.parentFile })
}

val distribution = configurations.create("distribution") {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(distribution.name, syncAlleyResources)
}
