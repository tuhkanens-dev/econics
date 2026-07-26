package dev.tuhkanens.econicsapi.api

import dev.tuhkanens.econicsapi.data.CurrencyCommandData
import dev.tuhkanens.econicsapi.data.CurrencyCommands
import dev.tuhkanens.econicsapi.data.CurrencyCommandsData
import dev.tuhkanens.econicsapi.data.CurrencyFileData
import dev.tuhkanens.econicsapi.data.CurrencyPermissionData
import org.jetbrains.exposed.v1.jdbc.Database
import java.math.BigDecimal

interface CurrencyFileAPI {
    fun getDatabase(currencyId: String): Database
    fun getCurrency(currencyId: String): CurrencyFileData?
    fun hasCurrency(currencyId: String): Boolean
    fun getName(currencyId: String): String?
    fun getDefaultAmount(currencyId: String): BigDecimal?
    fun getCommands(currencyId: String): CurrencyCommandsData?
    fun getCommand(currencyId: String, command: CurrencyCommands): CurrencyCommandData?
    fun getPermission(currencyId: String, command: CurrencyCommands): CurrencyPermissionData?
    fun getDecimalPattern(currencyId: String): String?
    fun getFormatDecimalPattern(currencyId: String, amount: BigDecimal): String?
}