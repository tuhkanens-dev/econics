package dev.tuhkanens.econicscore.database.base

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.tuhkanens.econicscore.database.EconicsDatabase
import dev.tuhkanens.econicscore.manager.ConfigManager
import org.jetbrains.exposed.v1.jdbc.Database

object MySQLDatabase : EconicsDatabase() {

    private var dataSource: HikariDataSource? = null
    private lateinit var database: Database

    override fun createConnection(): Database {
        val node = ConfigManager.get().node("database")

        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://${node.node("host").getString("localhost")}:${node.node("port").getInt(3306)}/${node.node("database").getString("econics")}?useSSL=false&characterEncoding=utf8"
            driverClassName = "com.mysql.cj.jdbc.Driver"
            username = node.node("user").getString("root") ?: "root"
            password = node.node("password").getString("") ?: ""
            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 30_000
            idleTimeout = 600_000
            maxLifetime = 1_800_000
        }

        val hikariDataSource = HikariDataSource(config)
        dataSource = hikariDataSource

        database = Database.connect(hikariDataSource)

        return database
    }

    override fun closeConnection() {
        dataSource?.close()
        dataSource = null
    }

    override fun getDatabase(): Database = database

}