package dev.tuhkanens.econicsapi.api

import dev.tuhkanens.econicsapi.data.PlayerCurrencyData
import dev.tuhkanens.econicsapi.result.EconicsResult
import java.math.BigDecimal
import java.util.UUID

interface PlayerCurrencyAPI {
    fun getPlayerCurrencies(uuid: UUID, local: Boolean = false): EconicsResult<List<PlayerCurrencyData>>
    fun addPlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): EconicsResult<Nothing>
    fun removePlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): EconicsResult<Nothing>
    fun setPlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): EconicsResult<Nothing>
    fun getPlayerCurrency(uuid: UUID, currencyId: String): EconicsResult<BigDecimal>
    fun hasPlayerCurrency(uuid: UUID, currencyId: String): EconicsResult<Nothing>
}
