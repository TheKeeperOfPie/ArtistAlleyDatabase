import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.intellij.lang.annotations.Language
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
    alias(libs.plugins.com.codingfeline.buildkonfig)
    alias(libs.plugins.dev.zacsweers.metro)
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
        implementation(projects.modules.utilsBuildConfig)
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

val sqlDelightWorkerOutput = configurations.create("sqlDelightWorkerOutput") {
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
    sqlDelightWorkerOutput(project(":modules:alley:sqldelight-worker")) {
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

val isDebug = project.hasProperty("debug")
val outputDir = if (isDebug) {
    "dist/web/developmentExecutable"
} else {
    "dist/web/productionExecutable"
}

buildkonfig {
    packageName = "com.thekeeperofpie.artistalleydatabase.alley.web.secrets"

    defaultConfigs {
        buildConfigField(
            type = FieldSpec.Type.BOOLEAN,
            name = "debug",
            value = isDebug.toString(),
            const = true,
        )
    }
}

val buildBothWebVariants = tasks.register<Sync>("buildBothWebVariants") {
    outputs.upToDateWhen { false }
    val alleyAppTask = if (isDebug) {
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

val copySqlDelightWorkerOutput = tasks.register<Copy>("copySqlDelightWorkerOutput") {
    outputs.upToDateWhen { false }
    mustRunAfter(buildBothWebVariants)
    from(sqlDelightWorkerOutput)
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
    val destDir =
        layout.buildDirectory.dir("$outputDir/composeResources/artistalleydatabase.modules.alley.data.generated.resources/files")
    into(destDir)
}

val copyAlleyFunctions = tasks.register<Copy>("copyAlleyFunctions") {
    outputs.upToDateWhen { false }
    mustRunAfter(buildBothWebVariants)
    from(alleyFunctionsOutput)
    include("*.mjs")
    rename {
        if (it.contains("alley-functions")) {
            "alley-functions.mjs"
        } else {
            it
        }
    }
    into(layout.buildDirectory.dir("$outputDir/functions/backend"))
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

val copyAlleyFunctionsMiddleware = tasks.register<Copy>("copyAlleyFunctionsMiddleware") {
    outputs.upToDateWhen { false }
    mustRunAfter(copyAlleyFunctions)

    from(alleyFunctionsMiddlewareOutput)
    include("*.mjs")
    rename {
        if (it.contains("middleware")) {
            "alley-functions-middleware.mjs"
        } else {
            it
        }
    }
    into(layout.buildDirectory.dir("$outputDir/functions/middleware"))
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

configurations.all {
    resolutionStrategy {
        capabilitiesResolution.withCapability("com.google.guava:listenablefuture") {
            select("com.google.guava:guava:0")
        }
    }
}

// Replicates Workbox InjectManifest since configuring that doesn't seem to work
tasks.register("buildSite") {
    outputs.upToDateWhen { false }
    dependsOn("exportLibraryDefinitions")
    dependsOn(":modules:alley:user:verifySqlDelightMigration")
    dependsOn(
        buildBothWebVariants,
        copyServiceWorkerOutput,
        copySqlDelightWorkerOutput,
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

        val miniflarePatchFileName = "miniflare+5.20261006.1-alpha.patch"
        val excludedFileNames = setOf(
            ".gitignore",
            "_headers",
            "_routes.json",
            "package.json",
            "serviceWorker.js",
            "wrangler.toml",
            miniflarePatchFileName,
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

        // Suffix with hash to avoid tracking revisions for immutable files that can be renamed
        val filesToCacheWithHashes = filesToCache
            .map { oldFile ->
                val hash = hash(oldFile)
                val (newFile, revision) = when {
                    // TODO: Can use immutable reference for worker when the hash is injected into
                    //  the Kotlin source file that references it
                    oldFile.name == "sqlDelightWorker.js" -> oldFile to hash
                    // Wasm file names are already hashes
                    oldFile.extension == "wasm" -> oldFile to null
                    // The 3 number js files fail to load when hashed
                    oldFile.nameWithoutExtension.length == 3 -> oldFile to hash
                    oldFile.extension == "js" ->
                        oldFile.resolveSibling("${oldFile.nameWithoutExtension}-$hash.${oldFile.extension}") to null
                    else -> oldFile to hash
                }
                Triple(oldFile, newFile, revision)
            }

        // Ensure scripts are read from site root
        val webJs = folder.resolve("alley-web.js")
        val webJsEdited = webJs.readText()
            .replace("\"originWasmAlley-web.js\"", "\"/originWasmAlley-web.js\"")
            .replace("\"originJsAlley-web.js\"", "\"/originJsAlley-web.js\"")
        webJs.writeText(webJsEdited)

        fun replaceFileContentWithHashedName(file: File) {
            var fileContent = file.readText()
            filesToCacheWithHashes.forEach { (oldFile, newFile, revision) ->
                if (revision == null) {
                    fileContent = if (oldFile.nameWithoutExtension.length == 3) {
                        // The 3 number .js files don't have .js in the source
                        fileContent.replace(
                            "(${oldFile.nameWithoutExtension})",
                            newFile.nameWithoutExtension
                        )
                    } else {
                        fileContent.replace(oldFile.name, newFile.name)
                    }
                }
            }
            file.writeText(fileContent)
        }

        listOfNotNull(
            "index.html",
            "sqlDelightWorker.js",
            "alley-web.js",
            "originWasmAlley-web.js",
            "originJsAlley-web.js",
            "alley-edit.js",
            "originWasmAlley-edit.js",
            "originJsAlley-edit.js",
            "alley-form.js",
            "originWasmAlley-form.js",
            "originJsAlley-form.js",
        )
            .map(folder::resolve)
            .filter(File::exists)
            .forEach(::replaceFileContentWithHashedName)

        // Rename after so that list above can use original file names
        filesToCacheWithHashes.forEach { (oldFile, newFile, revision) ->
            if (revision == null) {
                oldFile.renameTo(newFile)
            }
        }

        val cacheInput = filesToCacheWithHashes
            .filter { it.third == null }
            .joinToString(separator = "\\n") { (_, newFile) ->
                newFile.relativeTo(folder).path.replace(File.separatorChar, '/')
            }
        val cacheRevisionsInput = filesToCacheWithHashes
            .filter { it.third != null }
            .joinToString(separator = "\\n") { (_, newFile, revision) ->
                newFile.relativeTo(folder).path.replace(File.separatorChar, '/') + "-$revision"
            }
        val serviceWorker = folder.resolve("serviceWorker.js")
        val serviceWorkerEdited = serviceWorker.readText()
            .replace("CACHE_INPUT", cacheInput)
            .replace("CACHE_REVISIONS_INPUT", cacheRevisionsInput)
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

        val functionsFolder = folder.resolve("functions").apply { mkdir() }

        // TODO: Move this to a source file?
        val workerJs = functionsFolder.resolve("worker.mjs")

        @Language("javascript")
        val workerSourceCode = """
            import { Worker } from "./backend/alley-functions.mjs";
            import { Middleware } from "./middleware/alley-functions-middleware.mjs";

            export default {
                async fetch(request, env, ctx) {
                    const url = new URL(request.url);
                    if (url.pathname.startsWith("/edit/api") || url.pathname.startsWith("/form/api")) {
                        // Simulate the Pages Functions context
                        const context = {
                            request: request,
                            env: env,
                            functionPath: url.pathname,
                            data: ctx.data,
                            next: async (nextRequest) => {
                              const innerContext = {
                                request: nextRequest || request,
                                env: env,
                                functionPath: url.pathname,
                                data: context.data,
                              };
                              return Worker.request(innerContext);
                            }
                        };
                        return Middleware.request(context);
                    }
                    
                    return env.ASSETS.fetch(request);
                }
            };
            """
        workerJs.writeText(workerSourceCode.trimIndent())

        // TODO: Find better solution for this
        folder.resolve("form.html").copyTo(folder.resolve("form/artist.html"))

        val publicPath = folder.resolve("public").apply { mkdir() }.toPath()
        // TODO: Merge excludes with above
        val filesToKeepInRoot = setOf(
            ".gitignore",
            ".wrangler",
            "public",
            "functions",
            "wrangler",
            "package.json",
            "node_modules",
            miniflarePatchFileName,
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

        // Patch out the 25 MiB asset limit so that dev builds can avoid optimizing .wasm
        val patchesDir = folder.resolve("patches").apply { mkdir() }
        val targetPatchFile = patchesDir.resolve(miniflarePatchFileName)
        val patchFile = folder.resolve(miniflarePatchFileName)
        patchFile.renameTo(targetPatchFile)
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
