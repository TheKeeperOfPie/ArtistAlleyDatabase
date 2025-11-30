// Assumes that:
// - bun is available to execute bunx wrangler
// - user is logged into Cloudflare via wrangler to access D1/R2 instances
// - rclone is available on host to sync images
@file:OptIn(kotlin.time.ExperimentalTime::class)
@file:Suppress("CanConvertToMultiDollarString")

import java.io.File
import java.lang.ProcessBuilder
import java.util.Properties
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

val PROD = false
val REMOTE = true
val scriptDir = __FILE__.parentFile

val buildDir = scriptDir.resolve("build")
    .resolve(if (PROD) "prod" else "dev")
    .apply { mkdirs() }

val secretsFile = scriptDir.resolve("../../../alley-app/secrets.properties")
val secrets = Properties().apply { secretsFile.inputStream().use(::load) }
val wranglerToml = initializeWranglerFile(secrets)
val exportFile = buildDir.resolve("export.sql").apply { delete() }

runWranglerCommand(
    "d1",
    "export",
    "ARTIST_ALLEY_DB",
    "--no-schema",
    "--output",
    "\"${exportFile.absolutePath}\"",
)

if (PROD) {
    val snapshotTime = Clock.System.now().toString()
    runWranglerCommand(
        "r2",
        "object",
        "put",
        "--file",
        exportFile.absolutePath,
        "artist-alley-snapshots/$snapshotTime.sql"
    )
}

val dataDir = if (PROD) scriptDir.parentFile else buildDir
val targetFolder = dataDir.resolve("inputs/animeExpo2026").apply { mkdirs() }
exportFile.copyTo(targetFolder.resolve("database.sql"), overwrite = true)

val rcloneConf = buildDir.resolve("rclone.conf")
scriptDir.resolve("rclone.conf").copyTo(rcloneConf, overwrite = true)
rcloneConf.writeText(
    rcloneConf.readText()
        .replace("\$ACCESS_KEY_ID", secrets.getProperty("accessKeyId"))
        .replace("\$SECRET_ACCESS_KEY", secrets.getProperty("secretAccessKey"))
        .replace("\$ACCOUNT_ID", secrets.getProperty("cloudflareAccountId"))
)

if (REMOTE) {
    val imagesFolder = buildDir.resolve("images")
    runCommand(
        "rclone",
        "--config",
        rcloneConf.absolutePath,
        "sync",
        "\"Cloudflare R2:artist-alley-images\"",
        imagesFolder.absolutePath,
    )
}

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

fun initializeWranglerFile(secrets: Properties): File {
    val file = buildDir.resolve("wrangler.toml")
    val databaseId = secrets.getProperty("artistAlleyDatabaseId")
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
