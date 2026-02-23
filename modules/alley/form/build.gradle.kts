import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-kotlin")
    id("library-web")
    alias(libs.plugins.com.codingfeline.buildkonfig)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.jetBrainsAndroidX.navigation3.ui)

            implementation(libs.coil3.coil.compose)
            implementation(libs.jetBrainsCompose.material3.windowSizeClass)
            implementation(libs.composeunstyled.primitives)

            implementation(projects.modules.alley)
            implementation(projects.modules.alley.edit)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
        }
    }
}

val isWasmDebug = project.hasProperty("wasmDebug")

buildkonfig {
    packageName = "com.thekeeperofpie.artistalleydatabase.alley.form.secrets"

    defaultConfigs {
        buildConfigField(
            type = FieldSpec.Type.BOOLEAN,
            name = "isWasmDebug",
            value = isWasmDebug.toString(),
            const = true,
        )
    }
}
