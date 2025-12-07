
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.util.zip.CRC32

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.dev.zacsweers.metro)
    alias(libs.plugins.com.github.ben.manes.versions)
}

android {
    namespace = "com.thekeeperofpie.artistalley"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.thekeeperofpie.artistalley2025"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resourceConfigurations += "en"
    }

    val proguardFiles = (file("proguard/").listFiles().orEmpty().toList() +
            getDefaultProguardFile("proguard-android-optimize.txt")).toTypedArray()

    val debugKeystore = file(System.getProperty("user.home"))
        .resolve(".android")
        .resolve("debug.keystore")
    val debugKeystoreExists = debugKeystore.exists()

    if (debugKeystoreExists) {
        signingConfigs {
            create("default") {
                keyAlias = "androiddebugkey"
                keyPassword = "android"
                storeFile = debugKeystore
                storePassword = "android"
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            isCrunchPngs = false
            proguardFiles(*proguardFiles)

            if (debugKeystoreExists) {
                signingConfig = signingConfigs.getByName("default")
            }
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            proguardFiles(*proguardFiles)

            if (debugKeystoreExists) {
                signingConfig = signingConfigs.getByName("default")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            merges += "/META-INF/*"
            merges += "mozilla/public-suffix-list.txt"

            // Can happen if an archive was built incrementally and accidentally published as-is
            excludes += "**/previous-compilation-data.bin"
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.thekeeperofpie.artistalleydatabase.alley.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "com.thekeeperofpie.artistalley"
            packageVersion = "0.0.1"
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_18
        }
    }
    jvm("desktop")

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

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("nonServiceWorkerCommon") {
                withAndroidTarget()
                withJvm()
                withJs()
                withWasmJs()
            }
        }
    }

    compilerOptions {
        jvmToolchain(18)
        freeCompilerArgs.add("-Xcontext-receivers")
//        freeCompilerArgs.add("-Xwasm-use-new-exception-proposal")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(libs.coil3.coil.network.ktor3)
            implementation(libs.kotlinx.coroutines.core)
        }
        val nonServiceWorkerCommonMain by getting {
            dependencies {
                implementation(projects.modules.alley)
                implementation(projects.modules.alley.data)
                implementation(projects.modules.utils)
                implementation(projects.modules.utilsCompose)
                implementation(projects.modules.utilsInject)

                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)

                implementation(libs.kotlinx.serialization.json)

                implementation(libs.coil3.coil.compose)
                implementation(libs.jetBrainsAndroidX.navigation.compose)
                implementation(libs.jetBrainsAndroidX.navigationevent.compose)
                implementation(libs.kermit)
            }
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            runtimeOnly(libs.kotlinx.coroutines.android)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.java)
            }
        }
        val webMain by getting {
            dependsOn(nonServiceWorkerCommonMain)
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
        jsMain.dependencies {
            implementation(libs.jetBrainsCompose.ui.backhandler)
        }
        wasmJsMain.dependencies {
            implementation(libs.jetBrainsCompose.ui.backhandler)
        }
    }
}

val serviceWorkerOutput by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val alleyEditOutput by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val alleyFunctionsOutput by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val alleyFunctionsMiddlewareOutput by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    serviceWorkerOutput(project(":modules:alley-app:service-worker")) {
        targetConfiguration = "distribution"
    }
    alleyEditOutput(project(":modules:alley-edit")) {
        targetConfiguration = "distribution"
    }
    alleyFunctionsOutput(project(":modules:alley-functions")) {
        targetConfiguration = "distribution"
    }
    alleyFunctionsMiddlewareOutput(project(":modules:alley-functions:middleware")) {
        targetConfiguration = "distribution"
    }
}

val isWasmDebug = project.hasProperty("wasmDebug")
val outputDir = if (isWasmDebug) {
    "dist/web/developmentExecutable"
} else {
    "dist/web/productionExecutable"
}

val buildBothWebVariants by tasks.registering(Sync::class) {
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

val copyServiceWorkerOutput by tasks.registering(Copy::class) {
    mustRunAfter(buildBothWebVariants)
    from(serviceWorkerOutput)
    into(layout.buildDirectory.dir(outputDir))
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

val copyAlleyEdit by tasks.registering(Copy::class) {
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

val copyAlleyFunctions by tasks.registering(Copy::class) {
    mustRunAfter(buildBothWebVariants)
    from(alleyFunctionsOutput)
    include("*.mjs")
    rename {
        "/functions/database/" + if (it.contains("alley-functions")) {
            "[[database]].mjs"
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
            .resolve("functions/database/[[database]].mjs")
            .appendText(
                """
                export async function onRequest(context) {
                  return Worker.request(context)
                }
            """.trimIndent()
            )
    }
}

val copyAlleyFunctionsMiddleware by tasks.registering(Copy::class) {
    mustRunAfter(copyAlleyFunctions)

    // TODO: Does this need to manually dedupe with copyAlleyFunctions similar to copyAlleyEdit?
    from(alleyFunctionsMiddlewareOutput)
    include("*middleware.mjs")
    rename { "/functions/database/_middleware.mjs" }
    into(layout.buildDirectory.dir(outputDir))
    duplicatesStrategy = DuplicatesStrategy.FAIL

    val outputDir = project.layout.buildDirectory.dir(outputDir)
    doLast {
        // TODO: Expose onRequest directly and see if that just works
        outputDir.get().asFile
            .resolve("functions/database/_middleware.mjs")
            .appendText(
                """
                export async function onRequest(context) {
                  return Middleware.request(context)
                }
            """.trimIndent()
            )
    }
}

tasks.getByPath("preBuild").dependsOn(":copyGitHooks")

configurations.all {
    resolutionStrategy {
        capabilitiesResolution.withCapability("com.google.guava:listenablefuture") {
            select("com.google.guava:guava:0")
        }
    }
}

// Replicates Workbox InjectManifest since configuring that doesn't seem to work
tasks.register("webRelease") {
    outputs.upToDateWhen { false }
    dependsOn(":modules:alley:user:verifySqlDelightMigration")
    dependsOn(
        buildBothWebVariants,
        copyServiceWorkerOutput,
        copyAlleyEdit,
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
        val rootFiles = folder.listFiles()!!
            .filter { it.isFile }
            .filter { it.name != "serviceWorker.js" }
            .filter { it.name != "_headers" }
            .filter { it.extension.isNotBlank() }
            .filter { it.extension != "txt" }

        val icons = folder.resolve("icons")
            .listFiles()

        val resourceFiles = folder.resolve("composeResources")
            .walkTopDown()
            .onEnter {
                !it.path.contains("alley.edit.generated.resources") &&
                        !it.path.contains("alley_edit.generated.resources")
            }
            .filter { it.isFile }
            .filter {
                it.extension == "cvr" ||
                        it.extension == "ttf" ||
                        it.name.contains("database")
            }

        val alleyAppFiles = folder.resolve("alleyAppFiles.txt").readLines()
            .mapTo(mutableSetOf()) { folder.resolve(File(it)) }
        val alleyEditFiles = folder.resolve("alleyEditFiles.txt").readLines()
            .mapTo(mutableSetOf()) { folder.resolve(File(it)) }
        val editOnlyFiles = alleyEditFiles - alleyAppFiles

        val filesToCache = rootFiles + icons + resourceFiles - editOnlyFiles

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
            .replace("artistAlleyCacheKVId", properties.getProperty("artistAlleyCacheKVId"))
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
