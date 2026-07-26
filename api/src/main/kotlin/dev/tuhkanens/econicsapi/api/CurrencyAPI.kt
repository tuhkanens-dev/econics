package dev.tuhkanens.econicsapi.api

import dev.tuhkanens.econicsapi.data.CurrencyData
import dev.tuhkanens.econicsapi.data.CurrencyFileData
import dev.tuhkanens.econicsapi.result.EconicsResult
import java.util.concurrent.CompletableFuture

interface CurrencyAPI {
    fun addCurrency(currencyData: CurrencyFileData): CompletableFuture<EconicsResult<Nothing>>
    fun removeCurrency(currencyId: String): CompletableFuture<EconicsResult<Nothing>>
    fun getCurrency(currencyId: String): CompletableFuture<EconicsResult<CurrencyData>>
    fun hasCurrency(currencyId: String): CompletableFuture<EconicsResult<Nothing>>
    fun updateCurrencies()
}