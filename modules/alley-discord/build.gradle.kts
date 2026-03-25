import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
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
            implementation(npm("discord-interactions", "4.4.0"))
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
            .replace("discordBotClientSecret", properties.getProperty("discordBotClientSecret"))
            .replace("discordBotPublicKey", properties.getProperty("discordBotPublicKey"))
            .replace("discordBotToken", properties.getProperty("discordBotToken"))
            .replace("discordBotRedirectUrl", properties.getProperty("discordBotRedirectUrl"))
            .replace("discordBotVerifyUrl", properties.getProperty("discordBotVerifyUrl"))
        wranglerJson.writeText(wranglerJsonEdited)
    }
}
