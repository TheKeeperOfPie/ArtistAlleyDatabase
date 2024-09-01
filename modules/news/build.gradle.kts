plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
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
            implementation(project(":modules:utils"))
            implementation(project(":modules:utils-compose"))
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
