// Assumes that:
// - bun is available to execute bunx wrangler
// - user is logged into Cloudflare via wrangler to access D1/R2 instances
@file:OptIn(kotlin.time.ExperimentalTime::class)
@file:Suppress("CanConvertToMultiDollarString", "SimplifyBooleanWithConstants")

import java.io.File
import java.lang.ProcessBuilder
import java.util.Properties
import java.util.concurrent.TimeUnit
import kotlin.IllegalStateException
import kotlin.time.Clock

val RELEASE_FLAG = false
val PROD = false || RELEASE_FLAG
val PULL_REMOTE = false || RELEASE_FLAG
val WRITE_BACKUP = false || RELEASE_FLAG
val scriptDir = __FILE__.parentFile

val buildDir = scriptDir.resolve("build")
    .resolve(if (PROD) "prod" else "dev")
    .apply { mkdirs() }

val secretsFile = scriptDir.resolve("../../../alley-app/secrets.properties")
val secrets = Properties().apply { secretsFile.inputStream().use(::load) }
val wranglerToml = initializeWranglerFile(secrets)
val editExportFile = buildDir.resolve("editExport.sql")
val formExportFile = buildDir.resolve("formExport.sql")

val snapshotTime = Clock.System.now().toString().replace(":", ";")
val dataDir = if (PROD) scriptDir.parentFile else buildDir

if (PULL_REMOTE) {
    editExportFile.delete()
    runWranglerCommand(
        "d1",
        "export",
        "ARTIST_ALLEY_DB",
        "--output",
        "\"${editExportFile.absolutePath}\"",
    )
    editExportFile.copyTo(
        buildDir.resolve("snapshots/edit")
            .apply { mkdirs() }
            .resolve("$snapshotTime.sql")
    )
    editExportFile.copyTo(
        dataDir.resolve("inputs/snapshots/animeExpo2026")
            .apply { mkdirs() }
            .resolve("$snapshotTime.sql")
    )

    formExportFile.delete()
    runWranglerCommand(
        "d1",
        "export",
        "ARTIST_ALLEY_FORM_DB",
        "--output",
        "\"${formExportFile.absolutePath}\"",
    )
    formExportFile.copyTo(
        buildDir.resolve("snapshots/form")
            .apply { mkdirs() }
            .resolve("$snapshotTime.sql")
    )
}

dataDir.resolve("inputs/artists")
    .apply { mkdirs() }
    .resolve("animeExpo2026.sql")
    .writer()
    .use { writer ->
        editExportFile.useLines {
            it.filter { it.contains("\"artistEntryAnimeExpo2026\"") }
                .filterNot { it.contains("11111111-1111-1111-1111-111111111111") }
                .forEach(writer::appendLine)
        }
    }

dataDir.resolve("inputs/tags")
    .apply { mkdirs() }
    .resolve("tags.sql")
    .writer()
    .use { writer ->
        editExportFile.useLines {
            it.filter { it.contains("\"seriesEntry\"") || it.contains("\"merchEntry\"") }
                .map { it.replace("INSERT INTO", "INSERT OR REPLACE INTO") }
                .forEach(writer::appendLine)
        }
    }

if (WRITE_BACKUP) {
    runWranglerCommand(
        "r2",
        "object",
        "put",
        "--file",
        editExportFile.absolutePath,
        "artist-alley-snapshots/edit/$snapshotTime.sql"
    )
    runWranglerCommand(
        "r2",
        "object",
        "put",
        "--file",
        formExportFile.absolutePath,
        "artist-alley-snapshots/form/$snapshotTime.sql"
    )
}

fun runCommand(vararg params: String) {
    val process = ProcessBuilder(params.toList())
        .inheritIO()
        .redirectErrorStream(true)
        .start()
    val exited = process.waitFor(150, TimeUnit.SECONDS)
    if (!exited) {
        throw IllegalStateException("Command failed to exit")
    }

    val exitValue = process.exitValue()
    if (exitValue != 0) {
        throw IllegalStateException("Failed to run command: $exitValue")
    }
}

fun runWranglerCommand(vararg params: String) =
    runCommand(
        "bunx",
        "wrangler",
        "--config",
        wranglerToml.absolutePath,
        *params,
        "--remote",
    )

fun initializeWranglerFile(secrets: Properties): File {
    val file = buildDir.resolve("wrangler.toml")
    val artistAlleyDatabaseId = secrets.getProperty("artistAlleyDatabaseId")
    val artistAlleyFormDatabaseId = secrets.getProperty("artistAlleyFormDatabaseId")
    file.writeText(
        """
            name = "artistalley"
            compatibility_date = "2025-01-05"

            [[d1_databases]]
            database_id = "$artistAlleyDatabaseId"
            binding = "ARTIST_ALLEY_DB"
            database_name = "ARTIST_ALLEY_DB"
            preview_database_id = "ARTIST_ALLEY_DB"

            [[d1_databases]]
            database_id = "$artistAlleyFormDatabaseId"
            binding = "ARTIST_ALLEY_FORM_DB"
            database_name = "ARTIST_ALLEY_FORM_DB"
            preview_database_id = "ARTIST_ALLEY_FORM_DB"

            [[r2_buckets]]
            binding = "ARTIST_ALLEY_SNAPSHOTS_BUCKET"
            bucket_name = "artist-alley-snapshots"
        """.trimIndent()
    )
    return file
}
