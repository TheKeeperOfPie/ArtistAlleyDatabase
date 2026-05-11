import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("library-android")
    id("library-desktop")
    id("library-web")
    alias(libs.plugins.com.codingfeline.buildkonfig)
}

kotlin {
    android { namespace = "com.thekeeperofpie.artistalleydatabase.alley.models" }

    sourceSets {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        applyDefaultHierarchyTemplate()

        val jvmMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain { dependsOn(jvmMain) }
        desktopMain { dependsOn(jvmMain) }
        commonMain.dependencies {
            api("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.whyoleg.cryptography.core)
            implementation(libs.whyoleg.cryptography.provider.optimal)
        }
    }
}

val isWasmDebug = project.hasProperty("wasmDebug")

buildkonfig {
    packageName = "com.thekeeperofpie.artistalleydatabase.alley.models"

    defaultConfigs {
        buildConfigField(
            type = FieldSpec.Type.BOOLEAN,
            name = "isWasmDebug",
            value = isWasmDebug.toString(),
            const = true,
        )
    }
}
