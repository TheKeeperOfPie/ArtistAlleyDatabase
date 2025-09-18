import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-web")
    id("app.cash.sqldelight")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("web") {
                withJs()
                withWasmJs()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.modules.alley.data)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.kotlinx.io.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.sqlite)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.sqldelight.androidx.driver)
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.sqldelight.web.worker.driver.js)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.sqldelight.web.worker.driver.wasm.js)
            }
        }
        val webMain by getting {
            dependencies {
                implementation(npm("@thekeeperofpie/alley-sqldelight-worker", file("../sqldelight-worker")))
                implementation(npm("@sqlite.org/sqlite-wasm", "3.49.1-build2"))
            }
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.thekeeperofpie.artistalleydatabase.alley.user"
    }
}

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.user")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.1.0")
            version = 2
            generateAsync = true

            // :modules:alley:user:generateCommonMainAlleySqlDatabaseSchema
            schemaOutputDirectory = project.layout.projectDirectory.dir("src/commonMain/sqldelight/databases")
            verifyMigrations = true
        }
    }
}

compose.resources {
    publicResClass = true
}
