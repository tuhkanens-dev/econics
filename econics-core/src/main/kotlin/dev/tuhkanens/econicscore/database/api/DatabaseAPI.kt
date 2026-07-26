package dev.tuhkanens.econicscore.database.api

import org.jetbrains.exposed.v1.jdbc.Database

interface DatabaseAPI {
    fun connect()
    fun disconnect()
    fun getDatabase(): Database
}