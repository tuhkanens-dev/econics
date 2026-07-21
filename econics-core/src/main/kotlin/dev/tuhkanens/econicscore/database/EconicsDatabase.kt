package dev.tuhkanens.econicscore.database

import dev.tuhkanens.econicscore.database.table.CurrenciesTable
import dev.tuhkanens.econicscore.database.table.PlayerCurrenciesTable
import dev.tuhkanens.econicscore.database.table.PlayersTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

abstract class EconicsDatabase : DatabaseInterface {

    lateinit var db: Database

    protected abstract fun createConnection(): Database
    protected open fun closeConnection() {}

    override fun connect() {
        db = createConnection()
        transaction(db) {
            SchemaUtils.create(
                CurrenciesTable,
                PlayersTable,
                PlayerCurrenciesTable
            )
        }
    }

    override fun disconnect() {
        if (!::db.isInitialized) return
        closeConnection()
    }

}