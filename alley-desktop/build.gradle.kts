
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("com.mikepenz.aboutlibraries.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.dev.zacsweers.metro)
}

compose.desktop {
    application {
        mainClass = "com.thekeeperofpie.artistalleydatabase.alley.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "com.thekeeperofpie.artistalley"
            packageVersion = "0.0.1"
        }
    }
}

kotlin {
    jvm()

    applyDefaultHierarchyTemplate()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(libs.jetBrainsCompose.components.resources)
        implementation(libs.jetBrainsCompose.runtime)
        implementation(libs.coil3.coil.network.ktor3)
        implementation(libs.kotlinx.coroutines.core)
        implementation(projects.modules.alley)
        implementation(projects.modules.alley.data)
        implementation(projects.modules.utils)
        implementation(projects.modules.utilsCompose)
        implementation(projects.modules.utilsInject)

        implementation(libs.jetBrainsCompose.components.resources)
        implementation(libs.jetBrainsCompose.ui.tooling.preview)
        implementation(libs.jetBrainsCompose.foundation)
        implementation(libs.jetBrainsCompose.material3)
        implementation(libs.jetBrainsCompose.ui)

        implementation(libs.kotlinx.serialization.json)

        implementation(libs.coil3.coil.compose)
        implementation(libs.jetBrainsAndroidX.navigation3.ui)
        implementation(libs.jetBrainsAndroidX.navigationevent.compose)
        implementation(libs.kermit)
        implementation(this@kotlin.compose.desktop.currentOs)
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.ktor.client.java)
    }
}
