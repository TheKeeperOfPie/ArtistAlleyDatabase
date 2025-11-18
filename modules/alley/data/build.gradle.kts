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

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.data")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.1.0")
            generateAsync = true
        }
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
