package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import com.thekeeperofpie.artistalleydatabase.cloudflare.D1Database
import com.thekeeperofpie.artistalleydatabase.cloudflare.D1PreparedStatement
import com.thekeeperofpie.artistalleydatabase.cloudflare.WorkerSqlDriver
import kotlinx.coroutines.await

internal class ManualTransactionSqlDriver(
    private val driver: WorkerSqlDriver,
    private val database: D1Database,
) : SqlDriver by driver {

    private var currentTransaction: CloudflareTransaction? = null

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<R> = if (currentTransaction == null) {
        driver.executeQuery(identifier, sql, mapper, parameters, binders)
    } else {
        currentTransaction?.statements += driver.makeQuery(sql, parameters, binders)
        QueryResult.AsyncValue { mapper(EmptyCursor).await() }
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> = if (currentTransaction == null) {
        driver.execute(identifier, sql, parameters, binders)
    } else {
        currentTransaction?.statements += driver.makeQuery(sql, parameters, binders)
        QueryResult.AsyncValue { 0L }
    }

    override fun newTransaction(): QueryResult<Transacter.Transaction> {
        require(currentTransaction == null) {
            "Cannot nest transactions"
        }
        val transaction = CloudflareTransaction()
        currentTransaction = transaction
        return QueryResult.Value(transaction)
    }

    override fun currentTransaction(): Transacter.Transaction? = currentTransaction

    private inner class CloudflareTransaction : Transacter.Transaction() {
        // Unsupported
        override val enclosingTransaction: Transacter.Transaction? = null

        val statements = mutableListOf<D1PreparedStatement>()

        override fun endTransaction(successful: Boolean): QueryResult<Unit> =
            if (!successful || statements.isEmpty()) {
                QueryResult.AsyncValue {}
            } else {
                QueryResult.AsyncValue { database.batch(statements.toTypedArray()).await(); Unit }
            }.also { currentTransaction = null }
    }
}

private object EmptyCursor : SqlCursor {
    override fun next(): QueryResult<Boolean> = QueryResult.Value(false)
    override fun getString(index: Int) = null
    override fun getLong(index: Int) = null
    override fun getBytes(index: Int) = null
    override fun getDouble(index: Int) = null
    override fun getBoolean(index: Int) = null
}
