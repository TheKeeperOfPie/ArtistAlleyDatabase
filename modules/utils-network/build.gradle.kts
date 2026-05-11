plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
    id("library-web")
}

kotlin {
    sourceSets {
        val jvmMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.okhttp3.logging.interceptor)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.skrapeit)
            }
        }
        androidMain {
            dependsOn(jvmMain)
            dependencies {
                implementation(projects.modules.utilsBuildConfig)
            }
        }
        val desktopMain by getting {
            dependsOn(jvmMain)
        }
        commonMain.dependencies {
            api(libs.apollo.runtime)
            api(libs.ktor.client.core)
            api(libs.ktor.serialization.kotlinx.json)
            implementation(libs.apollo.engine.ktor)
            implementation(libs.ktor.client.content.negotiation)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.utils_network"
    }
}
