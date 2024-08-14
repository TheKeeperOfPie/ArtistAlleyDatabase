plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
}

kotlin {
    // TODO: This target is required to resolve kotlin-test properly, but why?
    iosX64()

    sourceSets {
        androidMain.dependencies {
            api(libs.ktor.client.okhttp)
            implementation(libs.xmlutil.serialization.android)
        }
        commonMain.dependencies {
            api(compose.components.resources)
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
