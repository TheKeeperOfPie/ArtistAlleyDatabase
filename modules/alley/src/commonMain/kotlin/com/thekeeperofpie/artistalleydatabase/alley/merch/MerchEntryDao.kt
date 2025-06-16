package com.thekeeperofpie.artistalleydatabase.alley.merch

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.GetMerchById
import com.thekeeperofpie.artistalleydatabase.alley.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.MerchQueries
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.getBooleanFixed
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

fun SqlCursor.toMerchWithUserData(): MerchWithUserData {
    val uuid = getString(1)!!
    return MerchWithUserData(
        merch = MerchEntry(
            name = getString(0)!!,
            uuid = uuid,
            notes = getString(2),
            categories = getString(3),
            has2024 = getBooleanFixed(4),
            has2025 = getBooleanFixed(5),
        ),
        userEntry = MerchUserEntry(
            merchId = uuid,
            favorite = getBooleanFixed(6),
        )
    )
}

fun GetMerchById.toMerchWithUserData() = MerchWithUserData(
    merch = MerchEntry(
        name = name,
        uuid = uuid,
        notes = notes,
        categories = categories,
        has2024 = has2024,
        has2025 = has2025,
    ),
    userEntry = MerchUserEntry(
        merchId = uuid,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )
)

@OptIn(ExperimentalCoroutinesApi::class)
class MerchEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val merchDao: suspend () -> MerchQueries = { database().merchQueries },
) {
    fun getMerchById(id: String) = flowFromSuspend { merchDao() }
        .flatMapLatest {
            it.getMerchById(id).asFlow().mapToOneOrNull(PlatformDispatchers.IO)
        }
        .mapLatest { it?.toMerchWithUserData() }

    fun getMerch(
        year: DataYear,
        favoriteOnly: Boolean = false,
    ): PagingSource<Int, MerchWithUserData> {
        val favoritePrefix = if (favoriteOnly) {
            "merchUserEntry.favorite = 1 AND "
        } else {
            ""
        }

        val joinStatement = """
            LEFT OUTER JOIN merchUserEntry
            ON merchEntry.uuid = merchUserEntry.merchId
        """.trimIndent()
        val countStatement = """
            SELECT COUNT(*) FROM merchEntry
            $joinStatement
            WHERE $favoritePrefix has${year.year} = 1
        """.trimIndent()
        val statement = """
            SELECT merchEntry.*, merchUserEntry.favorite FROM merchEntry
            $joinStatement
            WHERE $favoritePrefix has${year.year} = 1
            ORDER BY name COLLATE NOCASE
        """.trimIndent()
        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("merchEntry"),
            mapper = SqlCursor::toMerchWithUserData,
        )
    }

    suspend fun getMerchEntries(year: DataYear) = when (year) {
        DataYear.YEAR_2023 -> emptyList()
        DataYear.YEAR_2024 -> merchDao().getMerchEntries2024().awaitAsList()
        DataYear.YEAR_2025 -> merchDao().getMerchEntries2025().awaitAsList()
    }.filterNot { it.name.contains("Commissions") }

    fun searchMerch(
        year: DataYear,
        query: String,
        favoriteOnly: Boolean = false,
    ): PagingSource<Int, MerchWithUserData> {
        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)

        val yearFilter = when (year) {
            DataYear.YEAR_2023 -> ""
            DataYear.YEAR_2024 -> "has2024 = 1 AND "
            DataYear.YEAR_2025 -> "has2025 = 1 AND "
        }
        val likeAndQuery = yearFilter + DaoUtils.makeLikeAndQuery("merchEntry_fts.name", queries)

        val matchQuery = "'{ name } : $matchOrQuery'"

        val joinStatement = """
            LEFT OUTER JOIN merchUserEntry
            ON uuidAsKey = merchUserEntry.merchId
        """.trimIndent()

        val favoriteStatement = "WHERE merchUserEntry.favorite = 1"
            .takeIf { favoriteOnly }.orEmpty()

        val countStatement = DaoUtils.buildSearchCountStatement(
            ftsTableName = "merchEntry_fts",
            idField = "name",
            matchQuery = matchQuery,
            likeStatement = likeAndQuery,
            additionalSelectStatement = ", merchEntry_fts.uuid as uuidAsKey",
            additionalJoinStatement = joinStatement,
            andStatement = favoriteStatement,
        )
        val statement = DaoUtils.buildSearchStatement(
            tableName = "merchEntry",
            ftsTableName = "merchEntry_fts",
            select = "merchEntry.*, merchUserEntry.favorite",
            idField = "name",
            likeOrderBy = "ORDER BY merchEntry_fts.name COLLATE NOCASE",
            matchQuery = matchQuery,
            likeStatement = likeAndQuery,
            additionalSelectStatement = ", merchEntry_fts.uuid as uuidAsKey",
            additionalJoinStatement = joinStatement,
            andStatement = favoriteStatement,
        )

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("merchEntry_fts"),
            mapper = SqlCursor::toMerchWithUserData,
        )
    }
}
