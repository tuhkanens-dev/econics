package dev.tuhkanens.econicscore.api.implementation

import dev.tuhkanens.econicsapi.api.PlayerAPI
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.database.table.PlayerCurrenciesTable
import dev.tuhkanens.econicscore.database.table.PlayersTable
import dev.tuhkanens.econicscore.manager.DatabaseManager
import dev.tuhkanens.econicscore.utils.EconicsAsync
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
            EconicsAsync.runAsync {
                EconicsResult.GetSuccess(getPlayersSync(DatabaseManager.getCurrent().getDatabase()))
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun getLocalPlayers(): EconicsResult<List<UUID>> {
        return try {
            EconicsAsync.runAsync {
                EconicsResult.GetSuccess(getPlayersSync(DatabaseManager.getLocal().getDatabase()))
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun addPlayer(uuid: UUID, playerName: String): EconicsResult<Nothing> {
        return try {
            EconicsAsync.runAsync {
                addPlayerSync(uuid, playerName)
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun removePlayer(uuid: UUID): EconicsResult<Nothing> {
        return try {
            EconicsAsync.runAsync {
               removePlayerSync(uuid)
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun updatePlayerName(uuid: UUID, newPlayerName: String): EconicsResult<Nothing> {
        return try {
            EconicsAsync.runAsync {
                updatePlayerNameSync(uuid, newPlayerName)
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun hasPlayer(uuid: UUID): EconicsResult<Nothing> {
        return try {
            EconicsAsync.runAsync {
                if (existsPlayerSync(uuid, DatabaseManager.getCurrent().getDatabase()))
                    EconicsResult.Success
                else
                    EconicsResult.NotFound
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun hasLocalPlayer(uuid: UUID): EconicsResult<Nothing> {
        return try {
            EconicsAsync.runAsync {
                if (existsPlayerSync(uuid, DatabaseManager.getLocal().getDatabase()))
                    EconicsResult.Success
                else
                    EconicsResult.NotFound
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun ensurePlayer(uuid: UUID, playerName: String): EconicsResult<Nothing> {
        return try {
            EconicsAsync.runAsync {
                val currentDb = DatabaseManager.getCurrent().getDatabase()
                val localDb = DatabaseManager.getLocal().getDatabase()

                val existsCurrent = existsPlayerSync(uuid, currentDb)
                val existsLocal = existsPlayerSync(uuid, localDb)

                when {
                    existsCurrent && existsLocal -> {
                        updatePlayerNameSync(uuid, playerName)
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
                        addPlayerSync(uuid, playerName)
                    }
                }
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    private fun getPlayersSync(db: Database): List<UUID> {
        return transaction(db) {
            PlayersTable.selectAll().map { row ->
                UUID.fromString(row[PlayersTable.uuid])
            }
        }
    }

    private fun addPlayerSync(uuid: UUID, playerName: String): EconicsResult<Nothing> {
        val actualUuid = actualUuid(uuid)
        var overallResult: EconicsResult<Nothing> = EconicsResult.Success

        DatabaseManager.transactionBoth { db ->
            transaction(db.getDatabase()) {
                val exists = existsPlayerSync(uuid, db.getDatabase())
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

        return overallResult
    }

    private fun removePlayerSync(uuid: UUID): EconicsResult<Nothing> {
        var result: EconicsResult<Nothing> = EconicsResult.NotFound

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
                if (deleted > 0) result = EconicsResult.Success
            }
        }

        return result
    }

    private fun updatePlayerNameSync(uuid: UUID, newPlayerName: String): EconicsResult<Nothing> {
        var result: EconicsResult<Nothing> = EconicsResult.NotFound

        DatabaseManager.transactionBoth { db ->
            transaction(db.getDatabase()) {
                val updated = PlayersTable.update({ PlayersTable.uuid eq actualUuid(uuid) }) {
                    it[playerName] = newPlayerName
                }

                if (updated > 0) result = EconicsResult.Success
            }
        }

        return result
    }

    private fun existsPlayerSync(uuid: UUID, db: Database): Boolean {
        return transaction(db) {
            PlayersTable.selectAll()
                .where { PlayersTable.uuid eq actualUuid(uuid) }
                .singleOrNull() != null
        }
    }

    private fun actualUuid(uuid: UUID): String = uuid.toString()

}