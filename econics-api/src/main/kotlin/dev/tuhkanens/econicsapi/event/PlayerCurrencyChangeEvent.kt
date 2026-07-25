package dev.tuhkanens.econicsapi.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.math.BigDecimal
import java.util.UUID

class PlayerCurrencyChangeEvent(
    val uniqueId: UUID,
    val currencyId: String,
    val amount: BigDecimal
) : Event() {
    companion object {
        private val HANDLERS = HandlerList()
        @JvmStatic fun getHandlerList() = HANDLERS
    }

    override fun getHandlers(): HandlerList = HANDLERS
}