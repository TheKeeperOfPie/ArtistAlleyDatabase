plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-web")
    id("app.cash.sqldelight")
}

kotlin {
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
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.sqldelight.web.worker.driver.wasm.js)
                implementation(npm("@thekeeperofpie/alley-sqldelight-worker", file("../sqldelight-worker")))
                implementation(npm("@sqlite.org/sqlite-wasm", "3.49.1-build2"))
            }
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.alley.user"
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
