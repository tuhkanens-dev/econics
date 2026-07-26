package dev.tuhkanens.econicsapi.data

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