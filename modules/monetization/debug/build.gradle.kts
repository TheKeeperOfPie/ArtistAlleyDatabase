plugins {
    id("library-android")
    id("library-compose")
    id("library-inject")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.monetization.debug"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":modules:monetization"))
        }
    }
}
