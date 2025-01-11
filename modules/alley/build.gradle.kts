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
            api(projects.modules.entry)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)

            // TODO: This import doesn't work since 1.8.0-alpha01 isn't published for this artifact
//            implementation(compose.material3AdaptiveNavigationSuite)
            implementation("org.jetbrains.compose.material3:material3-adaptive-navigation-suite:1.7.3")
            implementation(libs.coil3.coil.compose)
            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.sqldelight.coroutines.extensions)
        }
        androidMain.dependencies {
            implementation(libs.androidx.sqlite)
            implementation(libs.sqldelight.android.driver)
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
                implementation(libs.okio.fakefilesystem)
                implementation(libs.sqldelight.web.worker.driver.wasm.js)
                implementation(devNpm("copy-webpack-plugin", "9.1.0"))
                implementation(npm("@thekeeperofpie/alley-sqldelight-worker", file("./sqldelight-worker")))
                implementation(npm("sql.js", "1.12.0"))
                implementation(npm("@sqlite.org/sqlite-wasm", "3.47.2-build1"))
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

dependencies {
    add("kspAndroid", kspProcessors.room.compiler)
    add("kspDesktop", kspProcessors.room.compiler)
}

val inputsTask = tasks.register<ArtistAlleyProcessInputsTask>("processArtistAlleyInputs")

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley")
            generateAsync = true
        }
    }
}

compose.resources {
    publicResClass = true
    customDirectory("commonMain", inputsTask.map { it.outputResources.get() })
}
