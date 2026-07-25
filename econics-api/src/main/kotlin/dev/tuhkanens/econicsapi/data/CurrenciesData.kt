package dev.tuhkanens.econicsapi.data

import java.math.BigDecimal

data class CurrencyFileData(
    val id: String,
    val name: String,
    val defaultAmount: BigDecimal,
    val decimalPattern: String,
    val localCurrency: Boolean = false,
    val commands: Map<CurrencyCommands, CommandData>
) {
    data class CommandData(
        val enabled: Boolean = true,
        val permission: PermissionData
    )

    data class PermissionData(
        val value: String,
        val required: Boolean = true
    )

    fun permissionData(currencyId: String, command: CurrencyCommands): PermissionData {
        return PermissionData("econics.$currencyId.${command.name.lowercase()}", true)
    }
}