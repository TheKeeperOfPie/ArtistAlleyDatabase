import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
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

    js {
        nodejs()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("com.thekeeperofpie.artistalleydatabase.shared:shared:0.0.1")
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.whyoleg.cryptography.core)
            implementation(libs.whyoleg.cryptography.provider.optimal)
            implementation(projects.modules.alley.data)
            implementation(projects.modules.alley.form.data)
            implementation(projects.modules.alley.models)
            implementation(projects.modules.cloudflare)
            implementation(npm("discord-interactions", "4.4.0"))
        }
    }
}

sqldelight {
    databases {
        create("AlleySqlDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.discord")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.2.1")
            generateAsync = true
            dependency(project(":modules:alley:data"))
            srcDirs(file("src/commonMain/sqldelight/alley"))
        }
        create("AlleyFormDatabase") {
            packageName.set("com.thekeeperofpie.artistalleydatabase.alley.discord.form")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.2.1")
            generateAsync = true
            dependency(project(":modules:alley:form:data"))
            srcDirs(file("src/commonMain/sqldelight/form"))
        }
    }
}

tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions {
        target.set("es2015")
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

val outputDir = "dist/web"
val buildTask = tasks.named("jsProductionExecutableCompileSync")

val syncOutput by tasks.registering(Sync::class) {
    outputs.upToDateWhen { false }
    from(buildTask)
    into(layout.buildDirectory.dir(outputDir))
    duplicatesStrategy = DuplicatesStrategy.FAIL

    val outputDir = project.layout.buildDirectory.dir(outputDir)
    doLast {
        outputDir.get().asFile
            .resolve("ArtistAlleyDatabase-modules-alley-discord.mjs")
            .appendText(
                """
                export default {
                    fetch(request, env) {
                        return Worker.request(request, env)
                    }
                }
            """.trimIndent()
            )
    }
}

val properties = Properties().apply {
    val secretsFile = projectDir.resolve("secrets.properties")
    if (secretsFile.exists()) {
        load(secretsFile.reader())
    }
}

buildkonfig {
    packageName = "com.thekeeperofpie.artistalleydatabase.alley.discord.secrets"

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

tasks.register("webRelease") {
    outputs.upToDateWhen { false }
    dependsOn(syncOutput)

    val outputDir = layout.buildDirectory.dir(outputDir)
    val propertiesFile = project.layout.projectDirectory.file("secrets.properties")
    doLast {
        val folder = outputDir.get().asFile
        val properties = Properties().apply { load(propertiesFile.asFile.reader()) }
        val wranglerJson = folder.resolve("wrangler.jsonc")
        val wranglerJsonEdited = wranglerJson.readText()
            .replace("discordBotAppID", properties.getProperty("discordBotAppID"))
            .replace("discordBotPublicKey", properties.getProperty("discordBotPublicKey"))
            .replace("discordBotRedirectUrl", properties.getProperty("discordBotRedirectUrl"))
            .replace("discordBotVerifyUrl", properties.getProperty("discordBotVerifyUrl"))
            .replace("discordGuildId", properties.getProperty("discordGuildId"))
            .replace("discordArtistRoleId", properties.getProperty("discordArtistRoleId"))
            .replace("discordArtistChannelId", properties.getProperty("discordArtistChannelId"))
            .replace("artistAlleyBotKvId", properties.getProperty("artistAlleyBotKvId"))
            .replace("artistAlleyDatabaseId", properties.getProperty("artistAlleyDatabaseId"))
            .replace("artistAlleyFormDatabaseId", properties.getProperty("artistAlleyFormDatabaseId"))
            .replace("artistAlleyUrl", properties.getProperty("artistAlleyUrl"))
        wranglerJson.writeText(wranglerJsonEdited)
    }
}
