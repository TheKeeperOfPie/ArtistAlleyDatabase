plugins {
    id("library-android")
    id("library-compose")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.monetization)
        }
    }
}
