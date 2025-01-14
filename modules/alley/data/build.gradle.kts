plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-web")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.uri.kmp)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.alley.data"
}

tasks.register<ArtistAlleyDatabaseTask>("generateArtistAlleyDatabase")
val inputsTask = tasks.register<ArtistAlleyProcessInputsTask>("processArtistAlleyInputs") {
    dependsOn("generateArtistAlleyDatabase")
}

compose.resources {
    publicResClass = true
    customDirectory("commonMain", inputsTask.map { it.outputResources.get() })
}
