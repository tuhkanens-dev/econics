package dev.tuhkanens.econicscore.api.implementation

import dev.tuhkanens.econicsapi.api.CurrencyFileAPI
import dev.tuhkanens.econicsapi.data.CurrencyAction
import dev.tuhkanens.econicsapi.data.CurrencyFileData
import dev.tuhkanens.econicscore.manager.CurrencyManager
import java.math.BigDecimal
import java.text.DecimalFormat

class CurrencyFileImpl : CurrencyFileAPI {

    override fun getCurrency(currencyId: String): CurrencyFileData? {
        return CurrencyManager.getCurrencies()[currencyId]
    }

    override fun hasCurrency(currencyId: String): Boolean {
        return getCurrency(currencyId) != null
    }

    override fun getName(currencyId: String): String? {
        return getCurrency(currencyId)?.name
    }

    override fun getDefaultAmount(currencyId: String): BigDecimal {
        return getCurrency(currencyId)?.defaultAmount ?: BigDecimal.ZERO
    }

    override fun getCommands(currencyId: String): Map<CurrencyAction, CurrencyFileData.CommandData>? {
        return getCurrency(currencyId)?.commands
    }

    override fun getCommand(currencyId: String, action: CurrencyAction): CurrencyFileData.CommandData? {
        return getCurrency(currencyId)?.commands[action]
    }
    override fun getPermission(currencyId: String, action: CurrencyAction): CurrencyFileData.PermissionData? {
        return getCommand(currencyId, action)?.permission
    }

    override fun getDecimalPattern(currencyId: String): String {
        return getCurrency(currencyId)?.decimalPattern ?: "#.##"
    }

    override fun getFormatDecimalPattern(currencyId: String, amount: BigDecimal): String {
        return DecimalFormat(getCurrency(currencyId)?.decimalPattern).format(amount) ?: amount.toPlainString()
    }

}