import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeNyc2024
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeNyc2025
import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.form.data.ArtistFormEntryHistory
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.build_logic.edit.BuildLogicEditDatabase
import com.thekeeperofpie.artistalleydatabase.build_logic.form.BuildLogicFormDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.VersionCatalogsExtension
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.CRC32
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal object Utils {

    internal val listStringAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String) =
            Json.decodeFromString<List<String>>(databaseValue)

        override fun encode(value: List<String>) = Json.encodeToString(value)
    }

    private val dataYearAdapter = object : ColumnAdapter<DataYear, String> {
        override fun decode(databaseValue: String) =
            DataYear.entries.first { it.serializedName == databaseValue }

        override fun encode(value: DataYear) = value.serializedName
    }

    private val listCatalogImageAdapter = object : ColumnAdapter<List<DatabaseImage>, String> {
        override fun decode(databaseValue: String) =
            Json.decodeFromString<List<DatabaseImage>>(databaseValue)

        override fun encode(value: List<DatabaseImage>) = Json.encodeToString(value)
    }

    private val artistStatusAdapter = object : ColumnAdapter<ArtistStatus, String> {
        override fun decode(databaseValue: String) =
            ArtistStatus.entries.find { it.name == databaseValue } ?: ArtistStatus.UNKNOWN

        override fun encode(value: ArtistStatus) = value.name
    }

    private val instantAdapter = object : ColumnAdapter<Instant, String> {
        override fun decode(databaseValue: String) = try {
            Instant.parse(databaseValue)
        } catch (_: IllegalArgumentException) {
            Instant.DISTANT_PAST
        }

        override fun encode(value: Instant) = value.toString()
    }

    val uuidAdapter = object : ColumnAdapter<Uuid, String> {
        override fun decode(databaseValue: String) = Uuid.parse(databaseValue)

        override fun encode(value: Uuid) = value.toString()
    }

    val tableMinAdapter = object : ColumnAdapter<TableMin, Long> {
        override fun decode(databaseValue: Long) = TableMin.parseFromValue(databaseValue.toInt())

        override fun encode(value: TableMin) = value.serializedValue.toLong()
    }

    private val embedsAdapter = object : ColumnAdapter<Map<String, DatabaseImage>, String> {
        override fun decode(databaseValue: String) =
            Json.decodeFromString<Map<String, DatabaseImage>>(databaseValue)

        override fun encode(value: Map<String, DatabaseImage>) = Json.encodeToString(value)
    }

    fun hash(file: File, vararg inputs: Any): Long {
        val crc32 = CRC32()
        inputs.forEach {
            crc32.update(it.toString().toByteArray())
        }
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                crc32.update(buffer, 0, bytesRead)
            }
        }
        return crc32.value
    }

    context(task: Task)
    fun readSqlFile(databaseFile: File, sqlFile: File): Boolean {
        if (!sqlFile.exists()) return false

        val process = ProcessBuilder(
            "sqlite3",
            databaseFile.absolutePath,
            "\".read \'${sqlFile.absolutePath}\'\""
        )
            .inheritIO()
            .redirectErrorStream(true)
            .start()
        val success = process.waitFor(30, TimeUnit.SECONDS)
        if (!success) {
            val errorText = process.inputStream.use {
                it.reader().use {
                    it.readText()
                }
            }
            task.logger.error("Failed to apply ${sqlFile.absolutePath}")
            errorText.lines().forEach(task.logger::error)
        }
        return success
    }

    fun createEditDatabase(dbFile: File): Pair<JdbcSqliteDriver, BuildLogicEditDatabase> {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        try {
            BuildLogicEditDatabase.Schema.create(driver)
        } catch (_: Throwable) {
            Thread.sleep(5000)
            BuildLogicEditDatabase.Schema.create(driver)
        }
        val database = BuildLogicEditDatabase(
            driver = driver,
            artistEntry2023Adapter = ArtistEntry2023.Adapter(
                artistNamesAdapter = listStringAdapter,
                linksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
            ),
            artistEntry2024Adapter = ArtistEntry2024.Adapter(
                linksAdapter = listStringAdapter,
                storeLinksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                seriesInferredAdapter = listStringAdapter,
                seriesConfirmedAdapter = listStringAdapter,
                merchInferredAdapter = listStringAdapter,
                merchConfirmedAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
                fallbackImageYearAdapter = dataYearAdapter,
            ),
            artistEntry2025Adapter = ArtistEntry2025.Adapter(
                linksAdapter = listStringAdapter,
                storeLinksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                seriesInferredAdapter = listStringAdapter,
                seriesConfirmedAdapter = listStringAdapter,
                merchInferredAdapter = listStringAdapter,
                merchConfirmedAdapter = listStringAdapter,
                commissionsAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
                fallbackImageYearAdapter = dataYearAdapter,
            ),
            artistEntryAnimeExpo2026Adapter = ArtistEntryAnimeExpo2026.Adapter(
                statusAdapter = artistStatusAdapter,
                socialLinksAdapter = listStringAdapter,
                storeLinksAdapter = listStringAdapter,
                portfolioLinksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                seriesInferredAdapter = listStringAdapter,
                seriesConfirmedAdapter = listStringAdapter,
                merchInferredAdapter = listStringAdapter,
                merchConfirmedAdapter = listStringAdapter,
                commissionsAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
                fallbackImageYearAdapter = dataYearAdapter,
                embedsAdapter = embedsAdapter,
                lastEditTimeAdapter = instantAdapter,
            ),
            artistEntryAnimeExpo2026ChangelogAdapter = ArtistEntryAnimeExpo2026Changelog.Adapter(
                artistIdAdapter = uuidAdapter,
                seriesInferredAdapter = listStringAdapter,
                seriesConfirmedAdapter = listStringAdapter,
                merchInferredAdapter = listStringAdapter,
                merchConfirmedAdapter = listStringAdapter,
            ),
            artistEntryAnimeNyc2024Adapter = ArtistEntryAnimeNyc2024.Adapter(
                linksAdapter = listStringAdapter,
                storeLinksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                seriesInferredAdapter = listStringAdapter,
                seriesConfirmedAdapter = listStringAdapter,
                merchInferredAdapter = listStringAdapter,
                merchConfirmedAdapter = listStringAdapter,
                commissionsAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
                fallbackImageYearAdapter = dataYearAdapter,
            ),
            artistEntryAnimeNyc2025Adapter = ArtistEntryAnimeNyc2025.Adapter(
                linksAdapter = listStringAdapter,
                storeLinksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                seriesInferredAdapter = listStringAdapter,
                seriesConfirmedAdapter = listStringAdapter,
                merchInferredAdapter = listStringAdapter,
                merchConfirmedAdapter = listStringAdapter,
                commissionsAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
                fallbackImageYearAdapter = dataYearAdapter,
            ),
            stampRallyEntry2023Adapter = StampRallyEntry2023.Adapter(
                tablesAdapter = listStringAdapter,
                linksAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
            ),
            stampRallyEntry2024Adapter = StampRallyEntry2024.Adapter(
                tablesAdapter = listStringAdapter,
                linksAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
                tableMinAdapter = tableMinAdapter,
            ),
            stampRallyEntry2025Adapter = StampRallyEntry2025.Adapter(
                tablesAdapter = listStringAdapter,
                linksAdapter = listStringAdapter,
                seriesAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
                tableMinAdapter = tableMinAdapter,
            ),
            stampRallyEntryAnimeExpo2026Adapter = StampRallyEntryAnimeExpo2026.Adapter(
                tablesAdapter = listStringAdapter,
                linksAdapter = listStringAdapter,
                seriesAdapter = listStringAdapter,
                merchAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
                lastEditTimeAdapter = instantAdapter,
                tableMinAdapter = tableMinAdapter,
            ),
            artistNotesAdapter = ArtistNotes.Adapter(
                dataYearAdapter = dataYearAdapter,
            ),
            artistUserEntryAdapter = ArtistUserEntry.Adapter(
                dataYearAdapter = dataYearAdapter,
            ),
            seriesEntryAdapter = SeriesEntry.Adapter(
                sourceAdapter = object : ColumnAdapter<SeriesSource, String> {
                    override fun decode(databaseValue: String) =
                        SeriesSource.entries.find { it.name == databaseValue }
                            ?: SeriesSource.NONE

                    override fun encode(value: SeriesSource) = value.name
                },
                synonymsAdapter = listStringAdapter,
            )
        )
        return driver to database
    }

    fun createFormDatabase(dbFile: File): Pair<JdbcSqliteDriver, BuildLogicFormDatabase> {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        try {
            BuildLogicFormDatabase.Schema.create(driver)
        } catch (_: Throwable) {
            Thread.sleep(5000)
            BuildLogicFormDatabase.Schema.create(driver)
        }
        val database = BuildLogicFormDatabase(
            driver = driver,
            artistFormEntryHistoryAdapter = ArtistFormEntryHistory.Adapter(
                artistIdAdapter = uuidAdapter,
                dataYearAdapter = dataYearAdapter,
                beforeSocialLinksAdapter = listStringAdapter,
                beforeStoreLinksAdapter = listStringAdapter,
                beforePortfolioLinksAdapter = listStringAdapter,
                beforeCatalogLinksAdapter = listStringAdapter,
                beforeCommissionsAdapter = listStringAdapter,
                beforeSeriesInferredAdapter = listStringAdapter,
                beforeSeriesConfirmedAdapter = listStringAdapter,
                beforeMerchInferredAdapter = listStringAdapter,
                beforeMerchConfirmedAdapter = listStringAdapter,
                beforeImagesAdapter = listCatalogImageAdapter,
                afterSocialLinksAdapter = listStringAdapter,
                afterStoreLinksAdapter = listStringAdapter,
                afterPortfolioLinksAdapter = listStringAdapter,
                afterCatalogLinksAdapter = listStringAdapter,
                afterCommissionsAdapter = listStringAdapter,
                afterSeriesInferredAdapter = listStringAdapter,
                afterSeriesConfirmedAdapter = listStringAdapter,
                afterMerchInferredAdapter = listStringAdapter,
                afterMerchConfirmedAdapter = listStringAdapter,
                afterImagesAdapter = listCatalogImageAdapter,
                timestampAdapter = instantAdapter,
            )
        )
        return driver to database
    }
}

fun Project.resolveLibraries(vararg names: String) = names.map {
    val libs = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
    try {
        libs.findLibrary(it.removePrefix("libs.").removePrefix("kspProcessors.")).get().get()
    } catch (t: Throwable) {
        throw IllegalArgumentException("Failed to find $it", t)
    }
}
