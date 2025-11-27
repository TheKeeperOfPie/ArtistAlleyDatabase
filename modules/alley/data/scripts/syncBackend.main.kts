// Assumes that:
// - bun is available to execute bunx wrangler
// - user is logged into Cloudflare via wrangler to access D1/R2 instances
@file:OptIn(kotlin.time.ExperimentalTime::class)

import java.io.File
import java.lang.ProcessBuilder
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

val REMOTE = true
val scriptDir = __FILE__.parentFile

val buildDir = scriptDir.resolve("build").apply { mkdir() }
val wranglerToml = initializeWranglerFile()
val exportFile = buildDir.resolve("export.sql").apply { delete() }

runWranglerCommand(
    "d1",
    "export",
    "ARTIST_ALLEY_DB",
    "--no-schema",
    "--output",
    "\"${exportFile.absolutePath}\"",
)

val snapshotTime = Clock.System.now().toString()
runWranglerCommand(
    "r2",
    "object",
    "put",
    "--file",
    exportFile.absolutePath,
    "artist-alley-snapshots/$snapshotTime.sql"
)

val targetFolder = scriptDir.parentFile.resolve("inputs/animeExpo2026").apply { mkdirs() }
exportFile.copyTo(targetFolder.resolve("database.sql"), overwrite = true)

fun runCommand(vararg params: String) =
    ProcessBuilder(params.toList())
        .inheritIO()
        .redirectErrorStream(true)
        .start()
        .waitFor(30, TimeUnit.SECONDS)

fun runWranglerCommand(vararg params: String, remote: Boolean = REMOTE) =
    runCommand(
        "bunx",
        "wrangler",
        "--config",
        wranglerToml.absolutePath,
        *params,
        if (remote) "--remote" else "--local",
    )

fun initializeWranglerFile(): File {
    val file = buildDir.resolve("wrangler.toml")
    val databaseId = scriptDir.resolve("../../../alley-app/secrets.properties")
        .readLines()
        .first()
        .removePrefix("artistAlleyDatabaseId=")
        .trim()
    file.writeText(
        """
            name = "artistalley"
            compatibility_date = "2025-01-05"

            [[d1_databases]]
            database_id = "$databaseId"
            binding = "ARTIST_ALLEY_DB"
            database_name = "ARTIST_ALLEY_DB"
            preview_database_id = "ARTIST_ALLEY_DB"

            [[r2_buckets]]
            binding = "ARTIST_ALLEY_IMAGES_BUCKET"
            bucket_name = "artist-alley-images"

            [[r2_buckets]]
            binding = "ARTIST_ALLEY_SNAPSHOTS_BUCKET"
            bucket_name = "artist-alley-snapshots"
        """.trimIndent()
    )
    return file
}
