plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(libs.androidx.security.crypto)
            api(libs.work.runtime)
            api(libs.work.runtime.ktx)
        }
        androidInstrumentedTest.dependencies {
            implementation(project(":modules:test-utils"))
            implementation(libs.androidx.junit.test)
            implementation(libs.androidx.test.runner)
            implementation(libs.junit.jupiter.api)
            implementation(libs.junit5.android.test.core)
            runtimeOnly(libs.junit5.android.test.runner)
        }
        commonMain.dependencies {
            api(project(":modules:anime"))
            api(project(":modules:art"))
            api(project(":modules:cds"))
            api(project(":modules:monetization"))
            api(project(":modules:secrets"))

            implementation(libs.coil3.coil.compose)
            implementation(libs.kermit)
            implementation(libs.lifecycle.viewmodel.compose)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.settings"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

compose.resources {
    publicResClass = true
}
