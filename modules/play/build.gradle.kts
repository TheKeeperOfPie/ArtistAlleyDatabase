plugins {
    id("library-android")
    id("library-compose")
    id("library-inject")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.play"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(projects.modules.monetization)
            implementation(libs.app.update.ktx)
            implementation(libs.billing.ktx)
        }
    }
}
