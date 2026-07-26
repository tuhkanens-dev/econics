package dev.tuhkanens.econicscore.manager

import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.database.api.DatabaseAPI
import dev.tuhkanens.econicscore.database.base.MySQLDatabase
import dev.tuhkanens.econicscore.database.base.SQLiteDatabase

object DatabaseManager {

    private val plugin = Main.plugin

    private lateinit var currentDatabase: DatabaseAPI
    private lateinit var localDatabase: DatabaseAPI

    fun connect() {
        if (::currentDatabase.isInitialized) {
            plugin.logger.warning("Database is already connected!")
            return
        }

        val config = ConfigManager.get()
        val provider = config.node("database", "provider").getString("sqlite")

        localDatabase = SQLiteDatabase
        localDatabase.connect()

        currentDatabase = when (provider) {
            "sqlite" -> SQLiteDatabase
            "mysql" -> MySQLDatabase
            else -> {
                plugin.logger.warning("Unknown database type: $provider, using SQLite")
                SQLiteDatabase
            }
        }

        if (currentDatabase !== localDatabase) {
            currentDatabase.connect()
        }
    }

    fun disconnect() {
        if (!::currentDatabase.isInitialized) return

        currentDatabase.disconnect()
        if (currentDatabase !== localDatabase) {
            localDatabase.disconnect()
        }

        plugin.logger.info("Databases connections closed")
    }

    inline fun <T> transactionBoth(
        block: (DatabaseAPI) -> T
    ): T {
        val current = getCurrent()
        val result = block(current)

        val local = getLocal()
        if (current !== local) {
            try {
                block(local)
            } catch (e: Exception) {
                Main.plugin.logger.warning("Failed to execute on local database: ${e.message}")
            }
        }

        return result
    }

    fun getCurrent(): DatabaseAPI = currentDatabase
    fun getLocal(): DatabaseAPI = localDatabase

}