package dev.tuhkanens.econicsapi.data

import java.math.BigDecimal

data class CurrencyFileData(
    val id: String,
    val name: String,
    val defaultAmount: BigDecimal,
    val decimalPattern: String,
    val localCurrency: Boolean = false,
    val commands: CurrencyCommandsData
)

data class CurrencyCommandsData(
    val enabled: Boolean = true,
    val commands: Map<CurrencyCommands, CurrencyCommandData>
) {
    companion object {
        fun default(currencyId: String): CurrencyCommandsData {
            val commands: MutableMap<CurrencyCommands, CurrencyCommandData> = mutableMapOf()
            for (command in CurrencyCommands.entries) {
                commands[command] = CurrencyCommandData.default(currencyId, command)
            }
            return CurrencyCommandsData(
                enabled = true,
                commands = commands
            )
        }
    }
}

data class CurrencyCommandData(
    val enabled: Boolean = true,
    val permission: CurrencyPermissionData
) {
    companion object {
        fun default(currencyId: String, command: CurrencyCommands): CurrencyCommandData {
            return CurrencyCommandData(
                enabled = true,
                permission = CurrencyPermissionData.default(currencyId, command)
            )
        }
    }
}

data class CurrencyPermissionData(
    val value: String,
    val required: Boolean = true
) {
    companion object {
        fun default(currencyId: String, command: CurrencyCommands): CurrencyPermissionData {
            return CurrencyPermissionData(
                value = "econics.$currencyId.${command.name.lowercase()}",
                required = true
            )
        }
    }
}