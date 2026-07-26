package dev.tuhkanens.econicscore.database.table

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object PlayersTable : IntIdTable("econics_players") {
    val uuid = varchar("uuid", 36).uniqueIndex()
    val playerName = varchar("player_name", 16)
}