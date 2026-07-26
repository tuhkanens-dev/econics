package dev.tuhkanens.econicsapi.data

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