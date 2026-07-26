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