plugins {
    id("library-android")
    id("library-compose")
    id("library-inject")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.monetization.unity"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(projects.modules.monetization)
            implementation(projects.modules.secrets)
            implementation(libs.unity.ads)
        }
    }
}
