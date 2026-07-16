plugins {
    id("library-android")
    id("library-desktop")
    id("library-inject")
    id("library-web")
}

kotlin {
    android {
        localDependencySelection {
            selectBuildTypeFrom.set(listOf("debug"))
        }
        androidResources {
            enable = true
        }
    }
}

kotlin {
    sourceSets {
        val jvmMain = create("jvmMain") {
            dependsOn(commonMain.get())
        }
        androidMain {
            dependsOn(jvmMain)
            dependencies {
                api(libs.androidx.security.crypto)
                implementation(projects.modules.utilsBuildConfig)
            }
        }
        commonMain.dependencies {
            api(libs.bignum)
            api(libs.kotlinx.io.core)
            api(libs.uri.kmp)
            implementation(libs.kotlinx.serialization.json.io)
        }
        named("desktopMain") {
            dependsOn(jvmMain)
            dependencies {
                implementation(libs.jimfs)
            }
        }
    }
}

kotlin {
    android {
        namespace = "com.thekeeperofpie.artistalleydatabase.utils"
    }
}
