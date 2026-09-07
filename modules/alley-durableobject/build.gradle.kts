
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.com.codingfeline.buildkonfig)
}

group = "com.thekeeperofpie.artistalleydatabase.alley.durableobject"

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
            implementation(projects.modules.cloudflare)
        }
    }
}

tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions {
        target.set("es2015")
    }
}

val distribution = configurations.create("distribution") {
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

val isDebug = project.hasProperty("debug")
val outputDir = if (isDebug) {
    "dist/web/development"
} else {
    "dist/web/production"
}
val buildTask = tasks.named("jsProductionExecutableCompileSync")

val syncOutput = tasks.register<Sync>("syncOutput") {
    outputs.upToDateWhen { false }
    from(buildTask)
    into(layout.buildDirectory.dir(outputDir))
    duplicatesStrategy = DuplicatesStrategy.FAIL

    filesMatching("**.mjs") { path = "src/$path" }
    filesMatching("**.mjs.map") { path = "src/$path" }

    val outputDir = project.layout.buildDirectory.dir(outputDir)
    doLast {
        outputDir.get().asFile
            .resolve("src/ArtistAlleyDatabase-modules-alley-durableobject.mjs")
            .appendText(
                """
                export default {
                    fetch(request, env) {
                        return handleRequest(request, env)
                    }
                }
            """.trimIndent()
            )
    }
}

tasks.register("webRelease") {
    outputs.upToDateWhen { false }
    dependsOn(syncOutput)
}
