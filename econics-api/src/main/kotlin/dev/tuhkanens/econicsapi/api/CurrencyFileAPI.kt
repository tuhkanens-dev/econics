package dev.tuhkanens.econicsapi.api

import dev.tuhkanens.econicsapi.data.CurrencyCommands
import dev.tuhkanens.econicsapi.data.CurrencyFileData
import org.jetbrains.exposed.v1.jdbc.Database
import java.math.BigDecimal

interface CurrencyFileAPI {
    fun getDatabase(currencyId: String): Database
    fun getCurrency(currencyId: String): CurrencyFileData?
    fun hasCurrency(currencyId: String): Boolean
    fun getName(currencyId: String): String?
    fun getDefaultAmount(currencyId: String): BigDecimal?
    fun getCommands(currencyId: String): Map<CurrencyCommands, CurrencyFileData.CommandData>?
    fun getCommand(currencyId: String, action: CurrencyCommands): CurrencyFileData.CommandData?
    fun getPermission(currencyId: String, action: CurrencyCommands): CurrencyFileData.PermissionData?
    fun getDecimalPattern(currencyId: String): String?
    fun getFormatDecimalPattern(currencyId: String, amount: BigDecimal): String?
}