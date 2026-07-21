package dev.tuhkanens.econicscore.api.implementation

import dev.tuhkanens.econicsapi.api.PlayerAPI
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.database.table.PlayersTable
import dev.tuhkanens.econicscore.manager.DatabaseManager
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class PlayerImpl : PlayerAPI {

    private val db: Database = DatabaseManager.getCurrentDatabase().getDatabase()

    override fun addPlayer(uuid: UUID, playerName: String): EconicsResult<Nothing> {
        return try {
            val actualUuid = actualUuid(uuid)

            transaction(db) {
                val exists = existsPlayer(actualUuid)
                if (!exists) {
                    PlayersTable.insert {
                        it[PlayersTable.uuid] = actualUuid
                        it[PlayersTable.playerName] = playerName
                    }
                    EconicsResult.Success
                } else {
                    EconicsResult.Already
                }
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun removePlayer(uuid: UUID): EconicsResult<Nothing> {
        return try {
            transaction(db) {
                val deleted = PlayersTable.deleteWhere { PlayersTable.uuid eq actualUuid(uuid) }
                if (deleted > 0) EconicsResult.Success else EconicsResult.NotFound
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun updatePlayerName(uuid: UUID, newPlayerName: String): EconicsResult<Nothing> {
        return try {
            transaction(db) {
                val updated = PlayersTable.update({ PlayersTable.uuid eq actualUuid(uuid) }) {
                    it[playerName] = newPlayerName
                }

                if (updated > 0) EconicsResult.Success else EconicsResult.NotFound
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun hasPlayer(uuid: UUID): EconicsResult<Nothing> {
        return try {
            transaction(db) {
                val exists = PlayersTable.selectAll()
                    .where { PlayersTable.uuid eq actualUuid(uuid) }
                    .singleOrNull() != null

                if (exists) EconicsResult.Success else EconicsResult.NotFound
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    private fun existsPlayer(uuid: String): Boolean {
        return PlayersTable.selectAll()
            .where { PlayersTable.uuid eq uuid }
            .singleOrNull() != null
    }

    private fun actualUuid(uuid: UUID): String = uuid.toString()

}