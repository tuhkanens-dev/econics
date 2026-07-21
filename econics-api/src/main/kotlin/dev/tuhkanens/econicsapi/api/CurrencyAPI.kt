package dev.tuhkanens.econicsapi.api

import dev.tuhkanens.econicsapi.data.CurrencyData
import dev.tuhkanens.econicsapi.data.CurrencyFileData
import dev.tuhkanens.econicsapi.result.EconicsResult

interface CurrencyAPI {
    fun addCurrency(currencyData: CurrencyFileData): EconicsResult<Nothing>
    fun removeCurrency(currencyId: String): EconicsResult<Nothing>
    fun getCurrency(currencyId: String): EconicsResult<CurrencyData>
    fun hasCurrency(currencyId: String): EconicsResult<Nothing>
}