package dev.tuhkanens.econicscore.api.implementation

import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyFileAPI
import dev.tuhkanens.econicsapi.api.PlayerCurrencyAPI
import dev.tuhkanens.econicsapi.data.PlayerCurrencyData
import dev.tuhkanens.econicsapi.event.PlayerCurrencyChangeEvent
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.database.table.CurrenciesTable
import dev.tuhkanens.econicscore.database.table.PlayerCurrenciesTable
import dev.tuhkanens.econicscore.database.table.PlayersTable
import dev.tuhkanens.econicscore.event.EventSuppressor
import dev.tuhkanens.econicscore.manager.DatabaseManager
import dev.tuhkanens.econicscore.utils.EconicsAsync
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.util.UUID

class PlayerCurrencyImpl : PlayerCurrencyAPI {

    private val plugin = Main.plugin

    private val api = EconicsAPI.getAPI<CurrencyFileAPI>()

    override fun getPlayerCurrencies(uuid: UUID, local: Boolean): EconicsResult<List<PlayerCurrencyData>> {
        return try {
            EconicsAsync.runAsync {
                val actualUuid = actualUuid(uuid)
                val db = if (local) DatabaseManager.getLocal() else DatabaseManager.getCurrent()

                val currencies = transaction(db.getDatabase()) {
                    val playerId = PlayersTable
                        .selectAll()
                        .where { PlayersTable.uuid eq actualUuid }
                        .singleOrNull()
                        ?.get(PlayersTable.id)
                        ?: return@transaction emptyList()

                    (PlayerCurrenciesTable innerJoin CurrenciesTable)
                        .selectAll()
                        .where { PlayerCurrenciesTable.player eq playerId }
                        .map {
                            PlayerCurrencyData(
                                id =  it[CurrenciesTable.id],
                                amount = it[PlayerCurrenciesTable.amount]
                            )
                        }
                }

                EconicsResult.GetSuccess(currencies)
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun addPlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): EconicsResult<Nothing> {
        return try {
            EconicsAsync.runAsync {
                changePlayerCurrency(uuid, currencyId) { current ->
                    (current ?: BigDecimal.ZERO).add(amount)
                }
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun removePlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): EconicsResult<Nothing> {
        return try {
            EconicsAsync.runAsync {
                changePlayerCurrency(uuid, currencyId) { current ->
                    (current ?: BigDecimal.ZERO).subtract(amount)
                }
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun setPlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): EconicsResult<Nothing> {
        return try {
            EconicsAsync.runAsync {
                changePlayerCurrency(uuid, currencyId) { amount }
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun getPlayerCurrency(uuid: UUID, currencyId: String): EconicsResult<BigDecimal> {
        return try {
            EconicsAsync.runAsync {
                val db = api.getDatabase(currencyId)
                transaction(db) {
                    val (playerId, currId) = getPlayerAndCurrencyIdsSync(uuid, currencyId)
                        ?: return@transaction EconicsResult.NotFound

                    val amount = getPlayerCurrencyAmountSync(playerId, currId)
                        ?: BigDecimal.ZERO

                    EconicsResult.GetSuccess(amount)
                }
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    override fun hasPlayerCurrency(uuid: UUID, currencyId: String): EconicsResult<Nothing> {
        return try {
            EconicsAsync.runAsync {
                val db = api.getDatabase(currencyId)
                transaction(db) {
                    val (playerId, currId) = getPlayerAndCurrencyIdsSync(uuid, currencyId)
                        ?: return@transaction EconicsResult.NotFound

                    val exists = getPlayerCurrencyAmountSync(playerId, currId) != null
                    if (exists) EconicsResult.Success else EconicsResult.NotFound
                }
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    private fun changePlayerCurrency(
        uuid: UUID,
        currencyId: String,
        calculator: (BigDecimal?) -> BigDecimal
    ): EconicsResult<Nothing> {
        return try {
            val db = api.getDatabase(currencyId)
            transaction(db) {
                val (playerId, currId) = getPlayerAndCurrencyIdsSync(uuid, currencyId)
                    ?: return@transaction EconicsResult.NotFound

                val current = getPlayerCurrencyAmountSync(playerId, currId)
                val newAmount = calculator(current)

                if (current == null) {
                    PlayerCurrenciesTable.insert {
                        it[player] = playerId
                        it[currency] = currId
                        it[PlayerCurrenciesTable.amount] = newAmount
                    }
                } else {
                    PlayerCurrenciesTable.update({
                        (PlayerCurrenciesTable.player eq playerId) and
                                (PlayerCurrenciesTable.currency eq currId)
                    }) {
                        it[PlayerCurrenciesTable.amount] = newAmount
                    }
                }

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    EventSuppressor.callEvent(PlayerCurrencyChangeEvent(uuid, currencyId, newAmount))
                })

                EconicsResult.Success
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.cause?.message ?: e.message ?: "Unknown error")
        }
    }

    private fun getPlayerAndCurrencyIdsSync(uuid: UUID, currencyId: String): Pair<EntityID<Int>, String>? {
        val playerId = PlayersTable.selectAll()
            .where { PlayersTable.uuid eq actualUuid(uuid) }
            .singleOrNull()?.get(PlayersTable.id) ?: return null

        val dbCurrencyId = CurrenciesTable.selectAll()
            .where { CurrenciesTable.id eq currencyId }
            .singleOrNull()?.get(CurrenciesTable.id) ?: return null

        return playerId to dbCurrencyId
    }

    private fun getPlayerCurrencyAmountSync(playerId: EntityID<Int>, currencyId: String): BigDecimal? {
        return PlayerCurrenciesTable.selectAll()
            .where { (PlayerCurrenciesTable.player eq playerId) and
                     (PlayerCurrenciesTable.currency eq currencyId) }
            .singleOrNull()
            ?.get(PlayerCurrenciesTable.amount)
    }

    private fun actualUuid(uuid: UUID): String = uuid.toString()

}