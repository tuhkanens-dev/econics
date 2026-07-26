package dev.tuhkanens.econicscore.database

import dev.tuhkanens.econicscore.database.api.DatabaseAPI
import dev.tuhkanens.econicscore.database.table.CurrenciesTable
import dev.tuhkanens.econicscore.database.table.PlayerCurrenciesTable
import dev.tuhkanens.econicscore.database.table.PlayersTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils

abstract class EconicsDatabase : DatabaseAPI {

    lateinit var db: Database

    protected abstract fun createConnection(): Database
    protected open fun closeConnection() {}

    private val tables = arrayOf(CurrenciesTable, PlayersTable, PlayerCurrenciesTable)

    override fun connect() {
        db = createConnection()

        transaction(db) {
            val statements = MigrationUtils.statementsRequiredForDatabaseMigration(*tables)
            if (statements.isNotEmpty()) {
                statements.forEach { exec(it) }
            } else {
                SchemaUtils.create(*tables)
            }
        }
    }

    override fun disconnect() {
        if (!::db.isInitialized) return
        closeConnection()
    }

}