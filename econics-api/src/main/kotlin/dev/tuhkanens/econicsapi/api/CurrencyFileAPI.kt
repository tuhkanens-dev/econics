package dev.tuhkanens.econicsapi.api

import dev.tuhkanens.econicsapi.data.CurrencyAction
import dev.tuhkanens.econicsapi.data.CurrencyFileData
import java.math.BigDecimal

interface CurrencyFileAPI {
    fun getCurrency(currencyId: String): CurrencyFileData?
    fun hasCurrency(currencyId: String): Boolean
    fun getName(currencyId: String): String?
    fun getDefaultAmount(currencyId: String): BigDecimal?
    fun getCommands(currencyId: String): Map<CurrencyAction, CurrencyFileData.CommandData>?
    fun getCommand(currencyId: String, action: CurrencyAction): CurrencyFileData.CommandData?
    fun getPermission(currencyId: String, action: CurrencyAction): CurrencyFileData.PermissionData?
    fun getDecimalPattern(currencyId: String): String?
    fun getFormatDecimalPattern(currencyId: String, amount: BigDecimal): String
}