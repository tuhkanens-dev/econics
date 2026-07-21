package dev.tuhkanens.econicscore.api.implementation

import dev.tuhkanens.econicsapi.api.PlayerCurrencyAPI
import dev.tuhkanens.econicsapi.data.PlayerCurrencyData
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.database.table.CurrenciesTable
import dev.tuhkanens.econicscore.database.table.PlayerCurrenciesTable
import dev.tuhkanens.econicscore.database.table.PlayersTable
import dev.tuhkanens.econicscore.manager.DatabaseManager
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.util.UUID

class PlayerCurrencyImpl : PlayerCurrencyAPI {

    private val db: Database = DatabaseManager.getCurrentDatabase().getDatabase()

    override fun getPlayerCurrencies(uuid: UUID): EconicsResult<List<PlayerCurrencyData>> {
        return try {
            val actualUuid = actualUuid(uuid)

            val currencies = transaction(db) {

                val playerId = PlayersTable
                    .selectAll()
                    .where { PlayersTable.uuid eq actualUuid }
                    .single()[PlayersTable.id]

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
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun addPlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): EconicsResult<Nothing> {
        return performCurrencyOperation(uuid, currencyId, amount) { _, newAmount ->
            newAmount
        }
    }

    override fun removePlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): EconicsResult<Nothing> {
        return performCurrencyOperation(uuid, currencyId, amount.negate()) { _, newAmount ->
            newAmount
        }
    }

    override fun setPlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): EconicsResult<Nothing> {
        return try {
            transaction(db) {
                val (playerId, currId) = getPlayerAndCurrencyIds(uuid, currencyId)
                    ?: return@transaction EconicsResult.NotFound

                val current = PlayerCurrenciesTable
                    .selectAll()
                    .where { (PlayerCurrenciesTable.player eq playerId) and
                            (PlayerCurrenciesTable.currency eq currId) }
                    .singleOrNull()

                if (current == null) {
                    PlayerCurrenciesTable.insert {
                        it[player] = playerId
                        it[currency] = currId
                        it[PlayerCurrenciesTable.amount] = amount
                    }
                } else {
                    PlayerCurrenciesTable.update({
                        (PlayerCurrenciesTable.player eq playerId) and (PlayerCurrenciesTable.currency eq currId)
                    }) {
                        it[PlayerCurrenciesTable.amount] = amount
                    }
                }

                EconicsResult.Success
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun getPlayerCurrency(uuid: UUID, currencyId: String): EconicsResult<BigDecimal> {
        return try {
            transaction(db) {
                val (playerId, currId) = getPlayerAndCurrencyIds(uuid, currencyId)
                    ?: return@transaction EconicsResult.NotFound

                val amount = getPlayerCurrencyAmount(playerId, currId)
                    ?: BigDecimal.ZERO

                EconicsResult.GetSuccess(amount)
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    override fun hasPlayerCurrency(uuid: UUID, currencyId: String): EconicsResult<Nothing> {
        return try {
            transaction(db) {
                val (playerId, currId) = getPlayerAndCurrencyIds(uuid, currencyId)
                    ?: return@transaction EconicsResult.NotFound

                val exists = getPlayerCurrencyAmount(playerId, currId) != null

                if (exists) EconicsResult.Success else EconicsResult.NotFound
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    private fun performCurrencyOperation(uuid: UUID, currencyId: String, delta: BigDecimal, newAmountCalculator: (BigDecimal, BigDecimal) -> BigDecimal = { _, new -> new }): EconicsResult<Nothing> {
        return try {
            transaction(db) {
                val (playerId, currId) = getPlayerAndCurrencyIds(uuid, currencyId)
                    ?: return@transaction EconicsResult.NotFound

                val current = PlayerCurrenciesTable
                    .selectAll()
                    .where { (PlayerCurrenciesTable.player eq playerId) and
                            (PlayerCurrenciesTable.currency eq currId) }
                    .singleOrNull()

                if (current == null) {
                    PlayerCurrenciesTable.insert {
                        it[player] = playerId
                        it[currency] = currId
                        it[PlayerCurrenciesTable.amount] = delta
                    }
                } else {
                    val currentAmount = current[PlayerCurrenciesTable.amount]
                    val newAmount = newAmountCalculator(currentAmount, currentAmount.add(delta))

                    PlayerCurrenciesTable.update({
                        (PlayerCurrenciesTable.player eq playerId) and (PlayerCurrenciesTable.currency eq currId)
                    }) {
                        it[PlayerCurrenciesTable.amount] = newAmount
                    }
                }

                EconicsResult.Success
            }
        } catch (e: Exception) {
            EconicsResult.Failure(e.message ?: "Unknown error")
        }
    }

    private fun getPlayerAndCurrencyIds(uuid: UUID, currencyId: String): Pair<EntityID<Int>, String>? {
        val playerRow = PlayersTable.selectAll()
            .where { PlayersTable.uuid eq actualUuid(uuid) }
            .singleOrNull() ?: return null

        val currencyRow = CurrenciesTable.selectAll()
            .where { CurrenciesTable.id eq currencyId }
            .singleOrNull() ?: return null

        return playerRow[PlayersTable.id] to currencyRow[CurrenciesTable.id]
    }

    private fun getPlayerCurrencyAmount(playerId: EntityID<Int>, currencyId: String): BigDecimal? {
        return PlayerCurrenciesTable.selectAll()
            .where { (PlayerCurrenciesTable.player eq playerId) and
                    (PlayerCurrenciesTable.currency eq currencyId) }
            .singleOrNull()?.get(PlayerCurrenciesTable.amount)
    }

    private fun actualUuid(uuid: UUID): String = uuid.toString()

}