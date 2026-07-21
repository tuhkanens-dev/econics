package dev.tuhkanens.econicsapi.data

import java.math.BigDecimal

data class CurrencyFileData(
    val id: String,
    val name: String,
    val defaultAmount: BigDecimal,
    val decimalPattern: String,
    val commands: Map<CurrencyAction, CommandData>
) {
    data class CommandData(
        val enabled: Boolean = true,
        val permission: PermissionData
    )

    data class PermissionData(
        val value: String,
        val required: Boolean = true
    )
}