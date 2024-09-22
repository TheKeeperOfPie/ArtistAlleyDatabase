plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(libs.ktor.client.okhttp)
            implementation(libs.xmlutil.serialization.android)
        }
        commonMain.dependencies {
            api(libs.kotlinx.io.core)
            api(libs.kotlinx.datetime)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(libs.xmlutil.serialization)
            implementation(libs.ktor.client.core)
            implementation(libs.kermit)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.news"
}
