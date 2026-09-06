import dev.zacsweers.metro.gradle.DelicateMetroGradleApi
import dev.zacsweers.metro.gradle.ExperimentalMetroGradleApi
import dev.zacsweers.metro.gradle.RequiresIdeSupport
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.nio.file.Files
import java.util.Properties
import java.util.zip.CRC32

plugins {
    id("com.mikepenz.aboutlibraries.plugin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.dev.zacsweers.metro)
}

@OptIn(DelicateMetroGradleApi::class, RequiresIdeSupport::class, ExperimentalMetroGradleApi::class)
metro {
    enableTopLevelFunctionInjection.set(false)
    generateContributionHintsInFir.set(false)
    supportedHintContributionPlatforms.set(emptySet())
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("ArtistAlleyWasm")
        browser {
            commonWebpackConfig {
                sourceMaps = false
            }
        }
        binaries.executable()
    }

    js {
        outputModuleName.set("ArtistAlleyJs")
        browser {
            commonWebpackConfig {
                sourceMaps = false
            }
        }
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()

    compilerOptions {
//        freeCompilerArgs.add("-Xwasm-use-new-exception-proposal")
    }

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
        implementation(libs.kotlinx.browser)
        implementation(libs.jetBrainsCompose.ui.backhandler)
    }
}

val serviceWorkerOutput = configurations.create("serviceWorkerOutput") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val alleyEditOutput = configurations.create("alleyEditOutput") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val alleyFormOutput = configurations.create("alleyFormOutput") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val alleyFunctionsOutput = configurations.create("alleyFunctionsOutput") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val alleyFunctionsMiddlewareOutput = configurations.create("alleyFunctionsMiddlewareOutput") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val alleyDataOutput = configurations.create("alleyDataOutput") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    serviceWorkerOutput(project(":modules:alley:service-worker")) {
        targetConfiguration = "distribution"
    }
    alleyEditOutput(project(":modules:alley-edit")) {
        targetConfiguration = "distribution"
    }
    alleyFormOutput(project(":modules:alley-form")) {
        targetConfiguration = "distribution"
    }
    alleyFunctionsOutput(project(":modules:alley-functions")) {
        targetConfiguration = "distribution"
    }
    alleyFunctionsMiddlewareOutput(project(":modules:alley-functions:middleware")) {
        targetConfiguration = "distribution"
    }
    alleyDataOutput(project(":modules:alley:data")) {
        targetConfiguration = "distribution"
    }
}

tasks.named("copyNonXmlValueResourcesForCommonMain").configure {
    dependsOn("exportLibraryDefinitions")
}

val isWasmDebug = project.hasProperty("wasmDebug")
val outputDir = if (isWasmDebug) {
    "dist/web/developmentExecutable"
} else {
    "dist/web/productionExecutable"
}

val buildBothWebVariants = tasks.register<Sync>("buildBothWebVariants") {
    outputs.upToDateWhen { false }
    val alleyAppTask = if (isWasmDebug) {
        "wasmJsBrowserDevelopmentExecutableDistribution"
    } else {
        "composeCompatibilityBrowserDistribution"
    }.let(tasks::named).get()
    dependsOn(alleyAppTask)

    val sourceFiles = alleyAppTask.outputs.files
    from(sourceFiles)
    val destDir = layout.buildDirectory.dir(outputDir)
    into(destDir)

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    doLast {
        Utils.writeCopiedFiles(sourceFiles, destDir, "alleyAppFiles.txt")
    }
}

val copyServiceWorkerOutput = tasks.register<Copy>("copyServiceWorkerOutput") {
    outputs.upToDateWhen { false }
    mustRunAfter(buildBothWebVariants)
    from(serviceWorkerOutput)
    into(layout.buildDirectory.dir(outputDir))
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

val copyAlleyEdit = tasks.register<Copy>("copyAlleyEdit") {
    outputs.upToDateWhen { false }
    mustRunAfter(buildBothWebVariants)

    val sourceFiles = alleyEditOutput.files
    from(alleyEditOutput)
    val destDir = layout.buildDirectory.dir(outputDir)
    into(destDir)

    val output = destDir.get().asFile

    // DuplicatesStrategy doesn't work for not overwriting buildBothWebVariants, manually exclude
    exclude {
        if (it.path.contains("composeResources/artistalleydatabase")) {
            if (
                !it.path.contains("artistalleydatabase.modules.alley_edit.generated.resources") &&
                !it.path.contains("artistalleydatabase.modules.alley.edit.generated.resources")
            ) {
                return@exclude true
            }
        }

        // This is really inefficient, but good enough since edit has a small number of files
        val sourceDir = sourceFiles.single()
        val alleyAppFiles = output.resolve("alleyAppFiles.txt")
            .readLines()
            .map { sourceDir.resolve(File(it)) }
            .toSet()
        it.file in alleyAppFiles
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    doLast {
        Utils.writeCopiedFiles(sourceFiles, destDir, "alleyEditFiles.txt")
    }
}

val copyAlleyForm = tasks.register<Copy>("copyAlleyForm") {
    outputs.upToDateWhen { false }
    mustRunAfter(buildBothWebVariants)

    val sourceFiles = alleyFormOutput.files
    from(alleyFormOutput)
    val destDir = layout.buildDirectory.dir(outputDir)
    into(destDir)

    val output = destDir.get().asFile

    // DuplicatesStrategy doesn't work for not overwriting buildBothWebVariants, manually exclude
    exclude {
        if (it.path.contains("composeResources/artistalleydatabase")) {
            if (
                !it.path.contains("artistalleydatabase.modules.alley_form.generated.resources") &&
                !it.path.contains("artistalleydatabase.modules.alley.form.generated.resources")
            ) {
                return@exclude true
            }
        }

        // This is really inefficient, but good enough since form has a small number of files
        val sourceDir = sourceFiles.single()
        val alleyAppFiles = output.resolve("alleyAppFiles.txt")
            .readLines()
            .map { sourceDir.resolve(File(it)) }
            .toSet()
        it.file in alleyAppFiles
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    doLast {
        Utils.writeCopiedFiles(sourceFiles, destDir, "alleyFormFiles.txt")
    }
}

val syncAlleyDataResources = tasks.register<Sync>("syncAlleyDataResources") {
    outputs.upToDateWhen { false }
    mustRunAfter(buildBothWebVariants)

    val syncTask = project(":modules:alley:data").tasks.named<Sync>("syncAlleyResources").get()
    dependsOn(syncTask)
    from(alleyDataOutput)
    val destDir = layout.buildDirectory.dir("$outputDir/composeResources/artistalleydatabase.modules.alley.data.generated.resources/files")
    into(destDir)
}

val copyAlleyFunctions = tasks.register<Copy>("copyAlleyFunctions") {
    outputs.upToDateWhen { false }
    mustRunAfter(buildBothWebVariants)
    from(alleyFunctionsOutput)
    include("*.mjs")
    rename {
        "/functions/" + if (it.contains("alley-functions")) {
            "[[catchall]].mjs"
        } else {
            it
        }
    }
    into(layout.buildDirectory.dir(outputDir))
    duplicatesStrategy = DuplicatesStrategy.FAIL

    val outputDir = project.layout.buildDirectory.dir(outputDir)
    doLast {
        // TODO: Expose onRequest directly and see if that just works
        outputDir.get().asFile
            .resolve("functions/[[catchall]].mjs")
            .appendText(
                """
                export async function onRequest(context) {
                  return Worker.request(context)
                }
            """.trimIndent()
            )
    }
}

val copyAlleyFunctionsMiddleware = tasks.register<Copy>("copyAlleyFunctionsMiddleware") {
    outputs.upToDateWhen { false }
    mustRunAfter(copyAlleyFunctions)

    // TODO: Does this need to manually dedupe with copyAlleyFunctions similar to copyAlleyEdit?
    from(alleyFunctionsMiddlewareOutput)
    include("*middleware.mjs")
    rename { "/functions/_middleware.mjs" }
    into(layout.buildDirectory.dir(outputDir))
    duplicatesStrategy = DuplicatesStrategy.FAIL

    val outputDir = project.layout.buildDirectory.dir(outputDir)
    doLast {
        // TODO: Expose onRequest directly and see if that just works
        outputDir.get().asFile
            .resolve("functions/_middleware.mjs")
            .appendText(
                """
                export async function onRequest(context) {
                  return Middleware.request(context)
                }
            """.trimIndent()
            )
    }
}

configurations.all {
    resolutionStrategy {
        capabilitiesResolution.withCapability("com.google.guava:listenablefuture") {
            select("com.google.guava:guava:0")
        }
    }
}

// Replicates Workbox InjectManifest since configuring that doesn't seem to work
tasks.register("buildRelease") {
    outputs.upToDateWhen { false }
    dependsOn("exportLibraryDefinitions")
    dependsOn(":modules:alley:user:verifySqlDelightMigration")
    dependsOn(
        buildBothWebVariants,
        copyServiceWorkerOutput,
        copyAlleyEdit,
        copyAlleyForm,
        syncAlleyDataResources,
        copyAlleyFunctions,
        copyAlleyFunctionsMiddleware,
    )

    val outputDir = project.layout.buildDirectory.dir(outputDir)
    val propertiesFile = project.layout.projectDirectory.file("secrets.properties")
    doLast {
        val folder = outputDir.get().asFile
        folder.listFiles()!!
            .filter { it.extension == "map" }
            .forEach { it.delete() }

        val excludedFileNames = setOf(
            ".gitignore",
            "_headers",
            "_routes.json",
            "package.json",
            "serviceWorker.js",
            "wrangler.toml",
        )
        val rootFiles = folder.listFiles()!!
            .filter { it.isFile }
            .filter { it.name !in excludedFileNames }
            .filter { it.extension.isNotBlank() }
            .filter { it.extension != "txt" }

        val resourceFiles = folder.resolve("composeResources")
            .walkTopDown()
            .onEnter {
                !it.path.contains("alley.edit.generated.resources") &&
                        !it.path.contains("alley_edit.generated.resources") &&
                        !it.path.contains("alley.form.generated.resources") &&
                        !it.path.contains("alley_form.generated.resources")
            }
            .filter { it.isFile }
            .filter {
                // .ttf is excluded since CJK fonts are considered non-critical
                it.extension == "cvr" || it.name.contains("database")
            }

        val alleyAppFiles = folder.resolve("alleyAppFiles.txt").readLines()
            .mapTo(mutableSetOf()) { folder.resolve(File(it)) }
        val alleyEditFiles = folder.resolve("alleyEditFiles.txt").readLines()
            .mapTo(mutableSetOf()) { folder.resolve(File(it)) }
        val alleyFormFiles = folder.resolve("alleyFormFiles.txt").readLines()
            .mapTo(mutableSetOf()) { folder.resolve(File(it)) }
        val editOrFormOnlyFiles = (alleyEditFiles + alleyFormFiles) - alleyAppFiles

        val filesToCache = rootFiles + resourceFiles - editOrFormOnlyFiles

        fun hash(file: File): Long {
            val crc32 = CRC32()
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    crc32.update(buffer, 0, bytesRead)
                }
            }
            return crc32.value
        }

        val fileNamesAndHashes = filesToCache
            .joinToString(separator = "\\n") {
                val relativePath = it.relativeTo(folder).path.replace(File.separatorChar, '/')
                "$relativePath-${hash(it)}"
            }
        val serviceWorker = folder.resolve("serviceWorker.js")
        val serviceWorkerEdited = serviceWorker.readText()
            .replace("CACHE_INPUT", fileNamesAndHashes)
        serviceWorker.writeText(serviceWorkerEdited)

        val properties = Properties().apply { load(propertiesFile.asFile.reader()) }
        val wranglerToml = folder.resolve("wrangler.toml")
        val wranglerTomlEdited = wranglerToml.readText()
            .replace("artistAlleyDatabaseId", properties.getProperty("artistAlleyDatabaseId"))
            .replace(
                "artistAlleyFormDatabaseId",
                properties.getProperty("artistAlleyFormDatabaseId")
            )
            .replace("artistAlleyCacheKVId", properties.getProperty("artistAlleyCacheKVId"))
            .replace("imagesAccessKeyId", properties.getProperty("imagesAccessKeyId"))
            .replace("imagesSecretAccessKeyId", properties.getProperty("imagesSecretAccessKeyId"))
            .replace("imagesCloudflareUrl", properties.getProperty("imagesCloudflareUrl"))
        wranglerToml.writeText(wranglerTomlEdited)

        // This is done here because syncing the site involves replacing all of the files in the
        // git repo, and so this file would be lost between builds.
        val txtFiles = folder.listFiles { it.extension == "txt" }!!.map { it.name }
        folder.resolve(".gitignore")
            .writeText(txtFiles.joinToString("\n") + "\n.wrangler")

        // Map key changes from webpackChunkalley_app to webpackChunkalley_edit and needs to be
        // manually consolidated into the same key
        val editJs = folder.resolve("alley-edit.js")
        val editJsEdited = editJs.readText()
            .replace("webpackChunkalley_edit", "webpackChunkalley_app")
        editJs.writeText(editJsEdited)

        listOf("alley-form.js", "originJsAlley-form.js", "originWasmAlley-form.js")
            .map(folder::resolve)
            .filter(File::exists)
            .forEach {
                val edited = it.readText()
                    .replace("webpackChunkalley_form", "webpackChunkalley_app")
                it.writeText(edited)
            }

        val webJs = folder.resolve("alley-web.js")
        val webJsEdited = webJs.readText()
            .replace("\"originWasmAlley-web.js\"", "\"/originWasmAlley-web.js\"")
            .replace("\"originJsAlley-web.js\"", "\"/originJsAlley-web.js\"")
        webJs.writeText(webJsEdited)

        val publicPath = folder.resolve("public").apply { mkdir() }.toPath()
        val filesToKeepInRoot = setOf(
            ".gitignore",
            ".wrangler",
            "public",
            "functions",
            "wrangler",
        )
        folder.listFiles()!!
            .filter { it.nameWithoutExtension !in filesToKeepInRoot && it.name !in filesToKeepInRoot }
            .forEach { Files.move(it.toPath(), publicPath.resolve(it.name)) }

        publicPath.resolve("composeResources").toFile()
            .listFiles()
            ?.forEach {
                if (it.list().isEmpty()) {
                    it.delete()
                }
            }
    }
}

private object Utils {
    fun writeCopiedFiles(
        sourceFiles: Iterable<File>,
        destDir: Provider<Directory>,
        outputFileName: String,
    ) {
        val sourceDir = sourceFiles.single()
        val files = sourceDir.listFiles()!!
            .filter { it.isFile }
            .joinToString("\n") { it.relativeTo(sourceDir).path }
        destDir.get().asFile.resolve(outputFileName).writeText(files)
    }
}
