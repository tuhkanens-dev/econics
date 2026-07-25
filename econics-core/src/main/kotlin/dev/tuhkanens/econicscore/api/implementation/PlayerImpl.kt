package dev.tuhkanens.econicscore.api.implementation

import dev.tuhkanens.econicsapi.api.PlayerAPI
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.database.table.PlayerCurrenciesTable
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

    override fun getPlayers(): EconicsResult<List<UUID>> {
        return try {
            EconicsResult.GetSuccess(getPlayers(DatabaseManager.getCurrent().getDatabase()))
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun getLocalPlayers(): EconicsResult<List<UUID>> {
        return try {
            EconicsResult.GetSuccess(getPlayers(DatabaseManager.getLocal().getDatabase()))
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    private fun getPlayers(db: Database): List<UUID> {
        return transaction(db) {
            PlayersTable.selectAll().map { row ->
                UUID.fromString(row[PlayersTable.uuid])
            }
        }
    }

    override fun addPlayer(uuid: UUID, playerName: String): EconicsResult<Nothing> {
        return try {
            val actualUuid = actualUuid(uuid)
            var overallResult: EconicsResult<Nothing> = EconicsResult.Success

            DatabaseManager.transactionBoth { db ->
                transaction(db.getDatabase()) {
                    val exists = existsPlayer(uuid, db.getDatabase())
                    if (!exists) {
                        PlayersTable.insert {
                            it[PlayersTable.uuid] = actualUuid
                            it[PlayersTable.playerName] = playerName
                        }
                        EconicsResult.Success
                    } else {
                        overallResult = EconicsResult.Already
                    }
                }
            }

            overallResult
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun removePlayer(uuid: UUID): EconicsResult<Nothing> {
        return try {
            DatabaseManager.transactionBoth { db ->
                transaction(db.getDatabase()) {
                    val playerId = PlayersTable.selectAll()
                        .where { PlayersTable.uuid eq actualUuid(uuid) }
                        .map { it[PlayersTable.id] }
                        .singleOrNull()

                    if (playerId != null) {
                        PlayerCurrenciesTable.deleteWhere { PlayerCurrenciesTable.player eq playerId }
                    }

                    val deleted = PlayersTable.deleteWhere { PlayersTable.uuid eq actualUuid(uuid) }
                    if (deleted > 0) EconicsResult.Success else EconicsResult.NotFound
                }
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun updatePlayerName(uuid: UUID, newPlayerName: String): EconicsResult<Nothing> {
        return try {
            DatabaseManager.transactionBoth { db ->
                transaction(db.getDatabase()) {
                    val updated = PlayersTable.update({ PlayersTable.uuid eq actualUuid(uuid) }) {
                        it[playerName] = newPlayerName
                    }

                    if (updated > 0) EconicsResult.Success else EconicsResult.NotFound
                }
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun hasPlayer(uuid: UUID): EconicsResult<Nothing> {
        return try {
            if (existsPlayer(uuid, DatabaseManager.getCurrent().getDatabase())) EconicsResult.Success else EconicsResult.NotFound
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun hasLocalPlayer(uuid: UUID): EconicsResult<Nothing> {
        return try {
            if (existsPlayer(uuid, DatabaseManager.getLocal().getDatabase())) EconicsResult.Success else EconicsResult.NotFound
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun ensurePlayer(uuid: UUID, playerName: String): EconicsResult<Nothing> {
        return try {
            val currentDb = DatabaseManager.getCurrent().getDatabase()
            val localDb = DatabaseManager.getLocal().getDatabase()

            val existsCurrent = existsPlayer(uuid, currentDb)
            val existsLocal = existsPlayer(uuid, localDb)

            when {
                existsCurrent && existsLocal -> {
                    updatePlayerName(uuid, playerName)
                    EconicsResult.Success
                }
                existsCurrent && !existsLocal -> {
                    transaction(localDb) {
                        PlayersTable.insert {
                            it[PlayersTable.uuid] = actualUuid(uuid)
                            it[PlayersTable.playerName] = playerName
                        }
                    }
                    EconicsResult.Success
                }
                !existsCurrent && existsLocal -> {
                    transaction(currentDb) {
                        PlayersTable.insert {
                            it[PlayersTable.uuid] = actualUuid(uuid)
                            it[PlayersTable.playerName] = playerName
                        }
                    }
                    EconicsResult.Success
                }
                else -> {
                    addPlayer(uuid, playerName)
                }
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    private fun existsPlayer(uuid: UUID, db: Database): Boolean {
        return transaction(db) {
            PlayersTable.selectAll()
                .where { PlayersTable.uuid eq actualUuid(uuid) }
                .singleOrNull() != null
        }
    }

    private fun actualUuid(uuid: UUID): String = uuid.toString()

}