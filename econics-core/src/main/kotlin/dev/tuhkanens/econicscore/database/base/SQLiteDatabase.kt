package dev.tuhkanens.econicscore.database.base

import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.database.EconicsDatabase
import org.jetbrains.exposed.v1.jdbc.Database
import java.io.File

object SQLiteDatabase : EconicsDatabase() {

    private val instance = Main.instance
    private lateinit var database: Database

    override fun createConnection(): Database {

        val dbFile = File("${instance.dataFolder}/econics.db")
        if (!dbFile.parentFile.exists()) dbFile.parentFile.mkdirs()

        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC"
        )

        return database
    }

    override fun getDatabase(): Database = database

}