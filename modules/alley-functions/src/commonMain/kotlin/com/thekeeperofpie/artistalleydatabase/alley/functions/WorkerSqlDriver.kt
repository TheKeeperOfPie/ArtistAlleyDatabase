@file:OptIn(ExperimentalJsCollectionsApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.D1Database
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

internal class WorkerSqlDriver(
    private val database: D1Database,
) : SqlDriver {
    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<R> {
        val statement = database.prepare(sql)
        val collectingStatement = CollectingStatement()
        binders?.invoke(collectingStatement)
        return QueryResult.AsyncValue {
            val result = statement.run {
                if (parameters > 0) {
                    bind(*collectingStatement.values.toTypedArray())
                } else {
                    this
                }
            }
                .raw()
                .await()
            mapper(WorkerSqlCursor(result)).await()
        }
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> {
        val statement = database.prepare(sql)
        val collectingStatement = CollectingStatement()
        binders?.invoke(collectingStatement)
        return QueryResult.AsyncValue {
            statement.run {
                if (parameters > 0) {
                    bind(*collectingStatement.values.toTypedArray())
                } else {
                    this
                }
            }
                .run()
                .await()
                .meta
                .changes
                .toLong()
        }
    }

    override fun newTransaction() = throw UnsupportedOperationException()

    override fun currentTransaction() = null

    override fun addListener(
        vararg queryKeys: String,
        listener: Query.Listener,
    ) {
    }

    override fun removeListener(
        vararg queryKeys: String,
        listener: Query.Listener,
    ) {
    }

    override fun notifyListeners(vararg queryKeys: String) {
    }

    override fun close() {
    }
}

internal class WorkerSqlCursor(private val values: Array<Array<dynamic>>) : SqlCursor {
    private var currentRow = -1

    override fun next(): QueryResult<Boolean> = QueryResult.Value(++currentRow < values.size)

    override fun getString(index: Int): String? = values[currentRow][index].unsafeCast<String?>()

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun getLong(index: Int): Long? = (values[currentRow][index] as? Double)?.toLong()

    override fun getBytes(index: Int): ByteArray? =
        (values[currentRow][index] as? Uint8Array)?.let { Int8Array(it.buffer).unsafeCast<ByteArray>() }

    override fun getDouble(index: Int): Double? = values[currentRow][index].unsafeCast<Double?>()

    override fun getBoolean(index: Int): Boolean? = values[currentRow][index].unsafeCast<Boolean?>()
}

/** D1 expects bindings to be provided as a vararg, extract them via this interceptor. */
private class CollectingStatement : SqlPreparedStatement {
    val values = mutableListOf<Any?>()

    override fun bindBytes(
        index: Int,
        bytes: ByteArray?,
    ) {
        values.add(bytes)
    }

    override fun bindDouble(
        index: Int,
        double: Double?,
    ) {
        values.add(double)
    }

    override fun bindLong(
        index: Int,
        long: Long?,
    ) {
        // D1 doesn't support Long
        values.add(long?.toInt())
    }

    override fun bindString(
        index: Int,
        string: String?,
    ) {
        values.add(string)
    }

    override fun bindBoolean(
        index: Int,
        boolean: Boolean?,
    ) {
        values.add(boolean)
    }
}
