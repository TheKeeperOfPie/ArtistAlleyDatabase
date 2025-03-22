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
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvm") {
                withAndroidTarget()
                withJvm()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.modules.alley.data)
            api(projects.modules.alley.user)
            api(projects.modules.entry)
            implementation(projects.modules.settings.ui)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)

            // TODO: This import doesn't work since 1.8.0-alpha01 isn't published for this artifact
//            implementation(compose.material3AdaptiveNavigationSuite)
            implementation("org.jetbrains.compose.material3:material3-adaptive-navigation-suite:1.8.0-beta01")
            implementation("org.jetbrains.compose.material3:material3-window-size-class:1.8.0-beta01")
            implementation(libs.coil3.coil.compose)
            implementation(libs.jetBrainsAndroidX.navigation.compose)
            implementation(libs.kotlinx.serialization.json.io)
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
        val jvmMain by getting {
            dependencies {
                implementation(libs.commons.csv)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.sqldelight.web.worker.driver.wasm.js)
                implementation(devNpm("copy-webpack-plugin", "9.1.0"))
                implementation(npm("@thekeeperofpie/alley-sqldelight-worker", file("./sqldelight-worker")))
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

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.0.2")
            generateAsync = true
            dependency(project(":modules:alley:user"))
        }
    }
}

compose.resources {
    publicResClass = true
}
