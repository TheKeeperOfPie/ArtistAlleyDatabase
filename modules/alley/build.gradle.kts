import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.util.Properties

plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-web")
    id("app.cash.sqldelight")
    alias(libs.plugins.com.codingfeline.buildkonfig)
    alias(libs.plugins.com.apollographql.apollo3.external)
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvm") {
                withAndroidTarget()
                withJvm()
            }
            group("web") {
                withJs()
                withWasmJs()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.modules.alley.data)
            api(projects.modules.alley.user)
            api(projects.modules.anilist.data)
            api(projects.modules.entry)
            api(projects.modules.utilsNetwork)
            implementation(projects.modules.apollo.utils)
            implementation(projects.modules.settings.ui)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)

            // TODO: This import doesn't work since 1.8.0-alpha01 isn't published for this artifact
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation("org.jetbrains.compose.material3:material3-window-size-class:1.9.0-alpha03")
            implementation(libs.coil3.coil.compose)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.human.readable)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
            implementation(libs.kotlinx.serialization.json.io)
            implementation(libs.qrose)
            implementation(libs.sqldelight.coroutines.extensions)
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
        val desktopTest by getting {
            dependencies {
                // TODO: Multiplatform variant doesn't resolve
                implementation(libs.paging.testing)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.commons.csv)
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
                implementation(devNpm("copy-webpack-plugin", "9.1.0"))
                implementation(
                    npm("@thekeeperofpie/alley-sqldelight-worker", file("./sqldelight-worker"))
                )
                implementation(npm("@sqlite.org/sqlite-wasm", "3.49.1-build2"))
            }
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.alley"
}

val properties = Properties().apply {
    load(projectDir.resolve("secrets.properties").reader())
}

buildkonfig {
    packageName = "com.thekeeperofpie.artistalleydatabase.alley.secrets"

    defaultConfigs {
        properties.forEach {
            buildConfigField(FieldSpec.Type.STRING, it.key.toString(), it.value.toString())
        }
    }
}

apollo {
    service("aniList") {
        packageName.set("com.anilist.data")
        dependsOn(project(":modules:anilist:data"))
        codegenModels.set("responseBased")
        decapitalizeFields.set(true)
        plugin(projects.modules.apollo)
    }
}

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.1.0")
            generateAsync = true
            dependency(project(":modules:alley:user"))
        }
    }
}

compose.resources {
    publicResClass = true
}
