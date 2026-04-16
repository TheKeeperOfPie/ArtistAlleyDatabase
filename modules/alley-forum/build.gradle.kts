
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("app.cash.sqldelight")
    alias(libs.plugins.com.codingfeline.buildkonfig)
}

group = "com.thekeeperofpie.artistalleydatabase.alley.discord"

kotlin {
    compilerOptions {
        optIn.addAll(
            "kotlin.time.ExperimentalTime",
            "kotlin.uuid.ExperimentalUuidApi",
        )
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
            implementation(compose.components.resources)
            implementation(compose.desktop.currentOs)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.kotlin.multiplatform.diff)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.java)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.sqldelight.sqlite.driver)
            implementation(projects.modules.alley)
            implementation(projects.modules.alley.data)
            implementation(projects.modules.alley.models)
            implementation(projects.modules.discord)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.thekeeperofpie.artistalleydatabase.alley.forum.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "com.thekeeperofpie.artistalleydatabase.alley.forum"
            packageVersion = "0.0.1"
        }
    }
}

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.forum")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.2.1")
            generateAsync = true
            dependency(project(":modules:alley:data"))
            srcDirs(file("src/commonMain/sqldelight"))
        }
    }
}

val properties = Properties().apply {
    val secretsFile = projectDir.resolve("secrets.properties")
    if (secretsFile.exists()) {
        load(secretsFile.reader())
    }
}

buildkonfig {
    packageName = "com.thekeeperofpie.artistalleydatabase.alley.forum.secrets"

    defaultConfigs {
        properties.forEach {
            buildConfigField(
                type = FieldSpec.Type.STRING,
                name = it.key.toString(),
                value = it.value.toString(),
                const = true,
            )
        }
    }
}
