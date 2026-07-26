package dev.tuhkanens.econicsapi.api

import dev.tuhkanens.econicsapi.data.PlayerCurrencyData
import dev.tuhkanens.econicsapi.result.EconicsResult
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface PlayerCurrencyAPI {
    fun getPlayerCurrencies(uuid: UUID, local: Boolean = false): CompletableFuture<EconicsResult<List<PlayerCurrencyData>>>
    fun addPlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): CompletableFuture<EconicsResult<Nothing>>
    fun removePlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): CompletableFuture<EconicsResult<Nothing>>
    fun setPlayerCurrency(uuid: UUID, currencyId: String, amount: BigDecimal): CompletableFuture<EconicsResult<Nothing>>
    fun getPlayerCurrency(uuid: UUID, currencyId: String): CompletableFuture<EconicsResult<BigDecimal>>
    fun hasPlayerCurrency(uuid: UUID, currencyId: String): CompletableFuture<EconicsResult<Nothing>>
}
