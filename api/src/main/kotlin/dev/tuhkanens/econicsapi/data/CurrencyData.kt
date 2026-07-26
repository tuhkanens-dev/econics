package dev.tuhkanens.econicsapi.data

import java.math.BigDecimal

data class CurrencyData(
    val id: String,
    val name: String,
    val defaultAmount: BigDecimal,
    val decimalPattern: String,
)