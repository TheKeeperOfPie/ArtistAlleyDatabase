import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("app.cash.sqldelight")
    alias(libs.plugins.com.codingfeline.buildkonfig)
}

group = "com.thekeeperofpie.artistalleydatabase.alley.functions"

kotlin {
    compilerOptions {
        optIn.addAll(
            "kotlin.uuid.ExperimentalUuidApi",
        )
    }

    js {
        nodejs()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
            implementation(libs.whyoleg.cryptography.core)
            implementation(libs.whyoleg.cryptography.provider.optimal)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.sqldelight.coroutines.extensions)

            implementation(projects.modules.alley.data)
            implementation(projects.modules.alley.models)
            implementation(npm("@cloudflare/pages-plugin-cloudflare-access", "1.0.5"))
            implementation(npm("@noble/curves", "2.0.1"))
            implementation(npm("aws4fetch", "1.0.20"))
        }
    }
}

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.functions")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.2.1")
            generateAsync = true
            dependency(project(":modules:alley:data"))
            srcDirs(file("src/commonMain/sqldelight/alley"))
        }
        create("AlleyFormDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.functions.form")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.2.1")
            generateAsync = true
            srcDirs(file("src/commonMain/sqldelight/form"))
        }
    }
}

val distribution: NamedDomainObjectProvider<Configuration> by configurations.registering {
    isCanBeConsumed = true
    isCanBeResolved = false
}

tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions {
        target.set("es2015")
    }
}

artifacts {
    add(distribution.name, tasks.named("jsProductionExecutableCompileSync"))
}


val isWasmDebug = project.hasProperty("wasmDebug")

buildkonfig {
    packageName = "com.thekeeperofpie.artistalleydatabase.alley.functions.secrets"

    defaultConfigs {
        buildConfigField(
            type = FieldSpec.Type.BOOLEAN,
            name = "debug",
            value = isWasmDebug.toString(),
            const = true
        )
    }
}
