package dev.tuhkanens.econicscore.database

import org.jetbrains.exposed.v1.jdbc.Database

interface DatabaseInterface {
    fun connect()
    fun disconnect()
    fun getDatabase(): Database
}