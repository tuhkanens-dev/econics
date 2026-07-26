package dev.tuhkanens.econicsapi.data

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