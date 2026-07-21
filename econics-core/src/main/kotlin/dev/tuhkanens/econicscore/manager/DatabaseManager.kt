package dev.tuhkanens.econicscore.manager

import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.database.DatabaseInterface
import dev.tuhkanens.econicscore.database.base.MySQLDatabase
import dev.tuhkanens.econicscore.database.base.SQLiteDatabase
import org.jetbrains.exposed.v1.jdbc.Database

object DatabaseManager {

    private val instance = Main.instance

    private lateinit var currentDatabase: DatabaseInterface

    fun connect() {
        if (::currentDatabase.isInitialized) {
            instance.logger.warning("Database is already connected!")
            return
        }

        val config = ConfigManager.get()
        val database = when (val provider = config.node("database", "provider").getString("sqlite")) {
            "sqlite" -> SQLiteDatabase
            "mysql" -> MySQLDatabase
            else -> {
                instance.logger.warning("Unknown database type: $provider, using SQLite")
                SQLiteDatabase
            }
        }

        database.connect()
        currentDatabase = database
    }

    fun disconnect() {
        if (!::currentDatabase.isInitialized) return

        currentDatabase.disconnect()
        instance.logger.info("Database connection closed")
    }

    fun getCurrentDatabase(): DatabaseInterface = currentDatabase

}