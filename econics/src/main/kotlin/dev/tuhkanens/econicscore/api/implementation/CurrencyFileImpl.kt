package dev.tuhkanens.econicscore.api.implementation

import dev.tuhkanens.econicsapi.api.CurrencyFileAPI
import dev.tuhkanens.econicsapi.data.CurrencyCommandData
import dev.tuhkanens.econicsapi.data.CurrencyCommands
import dev.tuhkanens.econicsapi.data.CurrencyCommandsData
import dev.tuhkanens.econicsapi.data.CurrencyFileData
import dev.tuhkanens.econicsapi.data.CurrencyPermissionData
import dev.tuhkanens.econicscore.manager.CurrencyManager
import dev.tuhkanens.econicscore.manager.DatabaseManager
import org.jetbrains.exposed.v1.jdbc.Database
import java.math.BigDecimal
import java.text.DecimalFormat

class CurrencyFileImpl : CurrencyFileAPI {

    override fun getDatabase(currencyId: String): Database {
        val localCurrency = getCurrency(currencyId)?.localCurrency == true
        return if (localCurrency) DatabaseManager.getLocal().getDatabase() else DatabaseManager.getCurrent().getDatabase()
    }

    override fun getCurrency(currencyId: String): CurrencyFileData? {
        return CurrencyManager.getCurrencies()[currencyId]
    }

    override fun hasCurrency(currencyId: String): Boolean {
        return getCurrency(currencyId) != null
    }

    override fun getName(currencyId: String): String? {
        return getCurrency(currencyId)?.name
    }

    override fun getDefaultAmount(currencyId: String): BigDecimal? {
        return getCurrency(currencyId)?.defaultAmount
    }

    override fun getCommands(currencyId: String): CurrencyCommandsData? {
        return getCurrency(currencyId)?.commands
    }

    override fun getCommand(currencyId: String, command: CurrencyCommands): CurrencyCommandData? {
        return getCommands(currencyId)?.commands[command]
    }

    override fun getPermission(currencyId: String, command: CurrencyCommands): CurrencyPermissionData? {
        return getCommand(currencyId, command)?.permission
    }

    override fun getDecimalPattern(currencyId: String): String? {
        return getCurrency(currencyId)?.decimalPattern
    }

    override fun getFormatDecimalPattern(currencyId: String, amount: BigDecimal): String? {
        val pattern = getCurrency(currencyId)?.decimalPattern ?: return null
        return DecimalFormat(pattern).format(amount)
    }

}