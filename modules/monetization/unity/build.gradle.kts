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
            api(project(":modules:monetization"))
            implementation(project(":modules:secrets"))
            implementation(libs.unity.ads)
        }
    }
}
